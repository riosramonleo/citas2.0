package com.rmmr.dating.messaging.api.dto;

import java.util.List;
import java.util.UUID;

public record InboxMetadataBatchResponse(List<Item> items) {
    public record Item(UUID matchId, MessageDto lastMessage, long unreadCount) {}
}