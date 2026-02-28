package com.rmmr.dating.interaction.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    boolean existsByFromUserIdAndToUserIdAndAction(String fromUserId, String toUserId, String action);
    Optional<Swipe> findByFromUserIdAndToUserId(String fromUserId, String toUserId);

    @Query("select s.toUserId from Swipe s where s.fromUserId = :fromUserId")
    List<String> findAllToUserIdsByFromUserId(@Param("fromUserId") String fromUserId);

}

