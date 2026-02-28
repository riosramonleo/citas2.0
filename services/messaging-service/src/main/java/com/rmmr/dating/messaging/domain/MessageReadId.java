package com.rmmr.dating.messaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class MessageReadId implements Serializable {

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    public MessageReadId() {}

    public MessageReadId(UUID matchId, String userId) {
        this.matchId = matchId;
        this.userId = userId;
    }

    public UUID getMatchId() { return matchId; }
    public String getUserId() { return userId; }

    // equals/hashCode necesarios para EmbeddedId
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageReadId other)) return false;
        return matchId.equals(other.matchId) && userId.equals(other.userId);
    }
    @Override public int hashCode() {
        return 31 * matchId.hashCode() + userId.hashCode();
    }
}