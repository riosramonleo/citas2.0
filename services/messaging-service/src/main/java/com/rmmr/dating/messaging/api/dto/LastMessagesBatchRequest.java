package com.rmmr.dating.messaging.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record LastMessagesBatchRequest(
        @NotEmpty List<UUID> matchIds
) {}