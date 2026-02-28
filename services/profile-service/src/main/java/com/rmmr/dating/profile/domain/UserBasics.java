package com.rmmr.dating.profile.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_basics")
public class UserBasics {

    @Id
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Gender gender;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "show_me", nullable = false, length = 16)
    private ShowMe showMe = ShowMe.EVERYONE;

    public ShowMe getShowMe() { return showMe; }
    public void setShowMe(ShowMe showMe) { this.showMe = showMe; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}