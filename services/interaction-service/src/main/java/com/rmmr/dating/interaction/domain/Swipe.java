package com.rmmr.dating.interaction.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "swipes",
        uniqueConstraints = @UniqueConstraint(name="ux_swipes_from_to", columnNames = {"from_user_id","to_user_id"})
)
public class Swipe {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="from_user_id", nullable=false, length=64)
    private String fromUserId;

    @Column(name="to_user_id", nullable=false, length=64)
    private String toUserId;

    @Column(name="action", nullable=false, length=16)
    private String action;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();

    protected Swipe() {}

    public Swipe(String fromUserId, String toUserId, String action) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.action = action;
    }

    public Long getId() { return id; }
    public String getFromUserId() { return fromUserId; }
    public String getToUserId() { return toUserId; }
    public String getAction() { return action; }
    public Instant getCreatedAt() { return createdAt; }
}