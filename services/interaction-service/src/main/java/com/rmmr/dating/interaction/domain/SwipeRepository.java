package com.rmmr.dating.interaction.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    boolean existsByFromUserIdAndToUserIdAndAction(String fromUserId, String toUserId, String action);
    Optional<Swipe> findByFromUserIdAndToUserId(String fromUserId, String toUserId);
}