package com.rmmr.dating.match.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    Optional<Match> findByUser1IdAndUser2Id(String user1Id, String user2Id);

    @Query("""
    select m from Match m
    where m.user1Id = :userId or m.user2Id = :userId
  """)
    List<Match> findAllForUser(String userId);

    List<Match> findByStateAndExpiresAtBefore(MatchState state, OffsetDateTime now);

    @Query("""
    select m from Match m
    where (m.user1Id = :userId or m.user2Id = :userId)
    order by m.createdAt desc
  """)
    List<Match> findAllForUserOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    @Query("""
    select m from Match m
    where (m.user1Id = :userId or m.user2Id = :userId)
      and m.state = :state
    order by m.createdAt desc
  """)
    List<Match> findAllForUserByStateOrderByCreatedAtDesc(
            @Param("userId") String userId,
            @Param("state") MatchState state,
            Pageable pageable
    );

}