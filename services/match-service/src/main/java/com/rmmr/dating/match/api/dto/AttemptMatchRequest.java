package com.rmmr.dating.match.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AttemptMatchRequest(
        @NotBlank String otherUserId
) {}