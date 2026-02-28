package com.rmmr.dating.profile.api.dto;

import com.rmmr.dating.profile.domain.Gender;
import jakarta.validation.constraints.NotNull;

public record UpsertBasicsRequest(
        @NotNull Gender gender
) {}