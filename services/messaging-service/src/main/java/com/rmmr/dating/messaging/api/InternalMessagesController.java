package com.rmmr.dating.messaging.api;

import com.rmmr.dating.messaging.api.dto.LastMessagesBatchRequest;
import com.rmmr.dating.messaging.api.dto.LastMessagesBatchResponse;
import com.rmmr.dating.messaging.api.dto.MessageDto;
import com.rmmr.dating.messaging.domain.MessageRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/messages")
public class InternalMessagesController {

    private final MessageRepository repo;

    public InternalMessagesController(MessageRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/last")
    public LastMessagesBatchResponse lastBatch(@Valid @RequestBody LastMessagesBatchRequest req) {
        // 1) Trae el último mensaje por match (1 query)
        var lastMessages = repo.findLastMessagesForMatchIds(req.matchIds());

        // 2) Map matchId -> MessageDto
        Map<UUID, MessageDto> map = lastMessages.stream()
                .collect(Collectors.toMap(
                        m -> m.getMatchId(),
                        m -> new MessageDto(m.getId(), m.getMatchId(), m.getSenderUserId(), m.getContent(), m.getCreatedAt())
                ));

        // 3) Responder en el mismo orden que pidieron (incluyendo null si no hay)
        var items = req.matchIds().stream()
                .map(id -> new LastMessagesBatchResponse.Item(id, map.get(id)))
                .toList();

        return new LastMessagesBatchResponse(items);
    }
}