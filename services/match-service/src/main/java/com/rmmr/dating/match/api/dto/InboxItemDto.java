package com.rmmr.dating.match.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InboxItemDto(
        UUID matchId,
        String otherUserId,
        String state,
        String firstMoverUserId,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        LastMessage lastMessage
) {
    public record LastMessage(
            UUID messageId,
            String senderUserId,
            String content,
            OffsetDateTime createdAt
    ) {}
}