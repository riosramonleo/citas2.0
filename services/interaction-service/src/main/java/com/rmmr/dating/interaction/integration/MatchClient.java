package com.rmmr.dating.interaction.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class MatchClient {

    private final RestClient restClient;

    public MatchClient(@Value("${clients.match-base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AttemptMatchResponse attemptMatch(String authorizationHeader, String otherUserId) {
        return restClient.post()
                .uri("/matches/attempt")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .body(new AttemptMatchRequest(otherUserId))
                .retrieve()
                .body(AttemptMatchResponse.class);
    }

    public record AttemptMatchRequest(String otherUserId) {}

    public record AttemptMatchResponse(
            boolean matched,
            UUID matchId,
            String state,
            OffsetDateTime expiresAt,
            String firstMoverUserId
    ) {}
}