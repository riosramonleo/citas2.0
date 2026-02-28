package com.rmmr.dating.messaging.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record InboxMetadataBatchRequest(@NotEmpty List<UUID> matchIds) {}