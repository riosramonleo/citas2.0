package com.rmmr.dating.match.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
    select e from OutboxEvent e
    where e.publishedAt is null
    order by e.createdAt asc
  """)
    List<OutboxEvent> findUnpublished(Pageable pageable);
}