package com.rmmr.dating.match.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivateMatchResponse(
        UUID matchId,
        String previousState,
        String state,
        OffsetDateTime activatedAt
) {}