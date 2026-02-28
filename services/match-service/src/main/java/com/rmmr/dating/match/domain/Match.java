package com.rmmr.dating.match.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "matches",
        uniqueConstraints = @UniqueConstraint(name = "uq_match_users", columnNames = {"user1_id", "user2_id"})
)
public class Match {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "user1_id", nullable = false, length = 64)
    private String user1Id;

    @Column(name = "user2_id", nullable = false, length = 64)
    private String user2Id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MatchState state;

    @Column(name = "first_mover_user_id", length = 64)
    private String firstMoverUserId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "activated_at")
    private OffsetDateTime activatedAt;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // getters/setters (rápido: genera con tu IDE)

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUser1Id() { return user1Id; }
    public void setUser1Id(String user1Id) { this.user1Id = user1Id; }

    public String getUser2Id() { return user2Id; }
    public void setUser2Id(String user2Id) { this.user2Id = user2Id; }

    public MatchState getState() { return state; }
    public void setState(MatchState state) { this.state = state; }

    public String getFirstMoverUserId() { return firstMoverUserId; }
    public void setFirstMoverUserId(String firstMoverUserId) { this.firstMoverUserId = firstMoverUserId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public OffsetDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(OffsetDateTime activatedAt) { this.activatedAt = activatedAt; }

    public OffsetDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(OffsetDateTime expiredAt) { this.expiredAt = expiredAt; }
}