package com.rmmr.dating.messaging.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "message_reads")
public class MessageRead {

    @EmbeddedId
    private MessageReadId id;

    @Column(name = "last_read_at", nullable = false)
    private OffsetDateTime lastReadAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public MessageRead() {}

    public MessageRead(MessageReadId id) {
        this.id = id;
        this.lastReadAt = OffsetDateTime.parse("1970-01-01T00:00:00Z");
    }

    @PrePersist
    @PreUpdate
    void touch() {
        if (lastReadAt == null) lastReadAt = OffsetDateTime.parse("1970-01-01T00:00:00Z");
        updatedAt = OffsetDateTime.now();
    }

    public MessageReadId getId() { return id; }
    public void setId(MessageReadId id) { this.id = id; }

    public OffsetDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(OffsetDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}