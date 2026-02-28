package com.rmmr.dating.match.api;

import com.rmmr.dating.match.api.dto.*;
import com.rmmr.dating.match.domain.MatchRepository;
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

    public MatchController(MatchAppService appService, MatchRepository repo) {
        this.appService = appService;
        this.repo = repo;
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
    public List<MatchDto> myMatches(@AuthenticationPrincipal Jwt jwt) {
        var list = repo.findAllForUser(jwt.getSubject());
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
}