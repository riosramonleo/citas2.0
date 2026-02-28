package com.rmmr.dating.match.jobs;

import com.rmmr.dating.match.service.MatchAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(MatchExpiryJob.class);
    private final MatchAppService service;

    public MatchExpiryJob(MatchAppService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "PT60S") // cada 60s (para MVP)
    public void expire() {
        int n = service.expireDueMatches();
        if (n > 0) log.info("Expired {} matches", n);
    }
}