package com.rmmr.dating.messaging.api;

import com.rmmr.dating.messaging.api.dto.MessageDto;
import com.rmmr.dating.messaging.api.dto.SendMessageRequest;
import com.rmmr.dating.messaging.domain.Message;
import com.rmmr.dating.messaging.domain.MessageRepository;
import com.rmmr.dating.messaging.integration.MatchClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.rmmr.dating.messaging.service.MessageAppService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/matches/{matchId}/messages")
public class MessageController {

    private final MessageRepository repo;
    private final MatchClient matchClient;
    private final MessageAppService messageAppService;

    public MessageController(MessageRepository repo, MatchClient matchClient, MessageAppService messageAppService) {
        this.repo = repo;
        this.matchClient = matchClient;
        this.messageAppService = messageAppService;
    }

    @PostMapping
    public MessageDto send(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID matchId,
            @Valid @RequestBody SendMessageRequest req
    ) {
        var saved = messageAppService.sendMessage(
                authorization,
                jwt.getSubject(),
                matchId,
                req.content()
        );

        return new MessageDto(
                saved.getId(),
                saved.getMatchId(),
                saved.getSenderUserId(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    /**
    @PostMapping
    public MessageDto send(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID matchId,
            @Valid @RequestBody SendMessageRequest req
    ) {
        // Bumble gatekeeper: activa o valida match
        matchClient.activateOrThrow(authorization, matchId);

        var m = new Message();
        m.setMatchId(matchId);
        m.setSenderUserId(jwt.getSubject());
        m.setContent(req.content());

        var saved = repo.save(m);

        return new MessageDto(
                saved.getId(),
                saved.getMatchId(),
                saved.getSenderUserId(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }**/

    @GetMapping
    public List<MessageDto> list(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID matchId
    ) {
        // 🔐 Seguridad: valida que el usuario es participante (403 si no)
        matchClient.getMatchOrThrow(authorization, matchId);

        return repo.findTop50ByMatchIdOrderByCreatedAtDesc(matchId).stream()
                .map(x -> new MessageDto(x.getId(), x.getMatchId(), x.getSenderUserId(), x.getContent(), x.getCreatedAt()))
                .toList();
    }
}