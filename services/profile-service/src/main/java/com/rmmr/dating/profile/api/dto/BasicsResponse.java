package com.rmmr.dating.profile.api.dto;

import com.rmmr.dating.profile.domain.Gender;
import com.rmmr.dating.profile.domain.ShowMe;

public record BasicsResponse(
        String userId,
        Gender gender,
        ShowMe showMe
) {}