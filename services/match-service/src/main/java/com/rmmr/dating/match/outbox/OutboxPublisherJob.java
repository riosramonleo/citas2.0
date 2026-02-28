package com.rmmr.dating.match.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);
    private final OutboxRepository repo;

    public OutboxPublisherJob(OutboxRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelayString = "PT5S")
    @Transactional
    public void publishBatch() {
        var batch = repo.findUnpublished(PageRequest.of(0, 50));
        for (var e : batch) {
            try {
                log.info("OUTBOX PUBLISH eventType={} aggregateId={} payload={}", e.getEventType(), e.getAggregateId(), e.getPayload());
                e.setPublishedAt(OffsetDateTime.now());
                e.setAttempts(e.getAttempts() + 1);
                e.setLastError(null);
            } catch (Exception ex) {
                e.setAttempts(e.getAttempts() + 1);
                e.setLastError(ex.getMessage());
            }
        }
        repo.saveAll(batch);
    }
}