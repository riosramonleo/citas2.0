package com.rmmr.dating.match.api.dto;

public record ActivateMatchRequest(
        String reason // opcional: "FIRST_MESSAGE_SENT"
) {}