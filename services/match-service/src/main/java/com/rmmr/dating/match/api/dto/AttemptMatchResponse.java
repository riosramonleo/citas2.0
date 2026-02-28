package com.rmmr.dating.match.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttemptMatchResponse(
        boolean matched,
        UUID matchId,
        String state,
        OffsetDateTime expiresAt,
        String firstMoverUserId
) {}