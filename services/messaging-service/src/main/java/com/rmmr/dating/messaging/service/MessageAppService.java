package com.rmmr.dating.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmmr.dating.messaging.domain.Message;
import com.rmmr.dating.messaging.domain.MessageRepository;
import com.rmmr.dating.messaging.integration.MatchClient;
import com.rmmr.dating.messaging.outbox.OutboxEvent;
import com.rmmr.dating.messaging.outbox.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageAppService {

    private final MessageRepository messageRepository;
    private final OutboxRepository outboxRepository;
    private final MatchClient matchClient;
    private final ObjectMapper objectMapper;

    public MessageAppService(
            MessageRepository messageRepository,
            OutboxRepository outboxRepository,
            MatchClient matchClient,
            ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.outboxRepository = outboxRepository;
        this.matchClient = matchClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Message sendMessage(String authorization, String senderUserId, UUID matchId, String content) {
        // 1) Validación Bumble (activa o verifica)
        matchClient.activateOrThrow(authorization, matchId);

        // 2) Guardar mensaje
        var m = new Message();
        m.setMatchId(matchId);
        m.setSenderUserId(senderUserId);
        m.setContent(content);
        var saved = messageRepository.save(m);

        // 3) Crear evento outbox (MessageSent)
        var event = new OutboxEvent();
        event.setAggregateType("Message");
        event.setAggregateId(saved.getId().toString());
        event.setEventType("MessageSent");
        event.setAttempts(0);
        event.setPayload(toJsonPayload(saved));

        outboxRepository.save(event);

        return saved;
    }

    private String toJsonPayload(Message saved) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("type", "MessageSent");
            payload.put("occurredAt", OffsetDateTime.now().toString());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("messageId", saved.getId().toString());
            data.put("matchId", saved.getMatchId().toString());
            data.put("senderUserId", saved.getSenderUserId());
            data.put("content", saved.getContent());
            data.put("createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null);

            payload.put("data", data);

            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build outbox payload", e);
        }
    }
}