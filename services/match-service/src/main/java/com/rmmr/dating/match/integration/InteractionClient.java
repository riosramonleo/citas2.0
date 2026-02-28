package com.rmmr.dating.match.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InteractionClient {

    private final RestClient restClient;

    public InteractionClient(@Value("${clients.interaction-base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean otherUserLikedMe(String authorizationHeader, String otherUserId) {
        var resp = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/swipes/match-check")
                        .queryParam("otherUserId", otherUserId)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(MatchCheckResponse.class);

        return resp != null && resp.likedMe();
    }

    public record MatchCheckResponse(String otherUserId, boolean likedMe) {}
}