package com.rmmr.dating.messaging.api;

import com.rmmr.dating.messaging.api.dto.InboxMetadataBatchRequest;
import com.rmmr.dating.messaging.api.dto.InboxMetadataBatchResponse;
import com.rmmr.dating.messaging.api.dto.MessageDto;
import com.rmmr.dating.messaging.domain.MessageRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/inbox")
public class InternalInboxController {

    private final MessageRepository repo;

    public InternalInboxController(MessageRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/metadata")
    public InboxMetadataBatchResponse metadata(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InboxMetadataBatchRequest req
    ) {
        String userId = jwt.getSubject();

        // last messages (1 query)
        var lastMessages = repo.findLastMessagesForMatchIds(req.matchIds());
        Map<UUID, MessageDto> lastMap = lastMessages.stream()
                .collect(Collectors.toMap(
                        m -> m.getMatchId(),
                        m -> new MessageDto(m.getId(), m.getMatchId(), m.getSenderUserId(), m.getContent(), m.getCreatedAt())
                ));

        // unread counts (1 query)
        var rows = repo.countUnreadByMatchIds(userId, req.matchIds());
        Map<UUID, Long> unreadMap = new HashMap<>();
        for (var r : rows) unreadMap.put(r.getMatchId(), r.getUnreadCount());

        // response in requested order
        var items = req.matchIds().stream()
                .map(mid -> new InboxMetadataBatchResponse.Item(
                        mid,
                        lastMap.get(mid),
                        unreadMap.getOrDefault(mid, 0L)
                ))
                .toList();

        return new InboxMetadataBatchResponse(items);
    }
}