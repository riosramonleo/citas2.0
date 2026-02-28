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

    @Query(value = """
      select m.match_id as matchId, count(*) as unreadCount
    from messages m
      left join message_reads r
        on r.match_id = m.match_id and r.user_id = :userId
    where m.match_id in (:matchIds)
        and m.sender_user_id <> :userId
        and m.created_at > coalesce(r.last_read_at, '1970-01-01 00:00:00+00'::timestamptz)
    group by m.match_id
    """, nativeQuery = true)
    List<UnreadCountRow> countUnreadByMatchIds(
            @Param("userId") String userId,
            @Param("matchIds") List<UUID> matchIds
    );

    interface UnreadCountRow {
        UUID getMatchId();
        long getUnreadCount();
    }

}