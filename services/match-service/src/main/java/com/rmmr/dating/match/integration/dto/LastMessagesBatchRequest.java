package com.rmmr.dating.match.integration.dto;

import java.util.List;
import java.util.UUID;

public record LastMessagesBatchRequest(List<UUID> matchIds) {}