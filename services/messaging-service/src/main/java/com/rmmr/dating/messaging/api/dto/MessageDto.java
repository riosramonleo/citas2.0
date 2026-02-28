package com.rmmr.dating.messaging.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID matchId,
        String senderUserId,
        String content,
        OffsetDateTime createdAt
) {}