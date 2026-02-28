package com.rmmr.dating.messaging.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop50ByMatchIdOrderByCreatedAtDesc(UUID matchId);
}