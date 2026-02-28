package com.rmmr.dating.match.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MatchSummaryDto(
        UUID id,
        String user1Id,
        String user2Id,
        String state,
        String firstMoverUserId,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {}