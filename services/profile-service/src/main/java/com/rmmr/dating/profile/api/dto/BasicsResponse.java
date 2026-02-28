package com.rmmr.dating.profile.api.dto;

import com.rmmr.dating.profile.domain.Gender;

public record BasicsResponse(
        String userId,
        Gender gender
) {}