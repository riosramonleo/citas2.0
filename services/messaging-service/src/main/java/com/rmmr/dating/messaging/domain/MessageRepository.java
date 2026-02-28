package com.rmmr.dating.messaging.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop50ByMatchIdOrderByCreatedAtDesc(UUID matchId);

    Optional<Message> findTop1ByMatchIdOrderByCreatedAtDesc(UUID matchId);

    @Query(value = """
  select distinct on (match_id) *
  from messages
  where match_id in (:matchIds)
  order by match_id, created_at desc
""", nativeQuery = true)
    List<Message> findLastMessagesForMatchIds(@Param("matchIds") List<UUID> matchIds);

}