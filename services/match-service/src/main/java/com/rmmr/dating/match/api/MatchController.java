package com.rmmr.dating.match.api;

import com.rmmr.dating.match.api.dto.*;
import com.rmmr.dating.match.domain.MatchRepository;
import com.rmmr.dating.match.domain.MatchState;
import com.rmmr.dating.match.integration.MessagingClient;
import com.rmmr.dating.match.service.MatchAppService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchAppService appService;
    private final MatchRepository repo;
    private final MessagingClient messagingClient;

    public MatchController(MatchAppService appService, MatchRepository repo, MessagingClient messagingClient) {
        this.appService = appService;
        this.repo = repo;
        this.messagingClient = messagingClient;
    }

    @GetMapping("/ping")
    public String ping(@AuthenticationPrincipal Jwt jwt) {
        return "match-service ok, sub=" + jwt.getSubject();
    }

    @PostMapping("/attempt")
    public AttemptMatchResponse attempt(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AttemptMatchRequest req
    ) {
        return appService.attempt(authorization, jwt.getSubject(), req.otherUserId());
    }

    @GetMapping("/me")
    public List<MatchDto> myMatches(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) MatchState state,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, Math.min(limit, 100));

        var list = (state == null)
                ? repo.findAllForUserOrderByCreatedAtDesc(jwt.getSubject(), pageable)
                : repo.findAllForUserByStateOrderByCreatedAtDesc(jwt.getSubject(), state, pageable);

        return list.stream()
                .map(m -> new MatchDto(
                        m.getId(),
                        m.getUser1Id(),
                        m.getUser2Id(),
                        m.getState().name(),
                        m.getFirstMoverUserId(),
                        m.getCreatedAt(),
                        m.getExpiresAt()
                ))
                .toList();
    }

    @PostMapping("/{id}/activate")
    public ActivateMatchResponse activate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) ActivateMatchRequest req
    ) {
        return appService.activate(id, jwt.getSubject());
    }

    @GetMapping("/{id}")
    public MatchSummaryDto getOne(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        return appService.getMatchForUser(id, jwt.getSubject());
    }

    @GetMapping("/inbox")
    public List<InboxItemDto> inbox(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, Math.min(limit, 100));

        // Usa tu query ordenado si ya lo agregaste; si no, usa findAllForUser(jwt.getSubject()) y luego sort.
        var matches = repo.findAllForUserOrderByCreatedAtDesc(jwt.getSubject(), pageable);

        var items = matches.stream().map(m -> {
            String me = jwt.getSubject();
            String other = me.equals(m.getUser1Id()) ? m.getUser2Id() : m.getUser1Id();

            var lastOpt = messagingClient.lastMessage(authorization, m.getId());
            InboxItemDto.LastMessage last = lastOpt
                    .map(x -> new InboxItemDto.LastMessage(x.id(), x.senderUserId(), x.content(), x.createdAt()))
                    .orElse(null);

            return new InboxItemDto(
                    m.getId(),
                    other,
                    m.getState().name(),
                    m.getFirstMoverUserId(),
                    m.getCreatedAt(),
                    m.getExpiresAt(),
                    last
            );
        }).toList();

        // Orden: por lastMessage.createdAt si existe, si no por createdAt del match
        return items.stream()
                .sorted((a, b) -> {
                    var ta = (a.lastMessage() != null && a.lastMessage().createdAt() != null)
                            ? a.lastMessage().createdAt()
                            : a.createdAt();
                    var tb = (b.lastMessage() != null && b.lastMessage().createdAt() != null)
                            ? b.lastMessage().createdAt()
                            : b.createdAt();
                    return tb.compareTo(ta); // desc
                })
                .toList();
    }

}