package com.rmmr.dating.match.integration.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record LastMessagesBatchResponse(
        List<Item> items
) {
    public record Item(UUID matchId, LastMessageDto lastMessage) {}

    public record LastMessageDto(
            UUID id,
            UUID matchId,
            String senderUserId,
            String content,
            OffsetDateTime createdAt
    ) {}
}