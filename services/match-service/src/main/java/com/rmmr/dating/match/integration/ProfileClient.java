package com.rmmr.dating.match.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProfileClient {

    private final RestClient rest;

    public ProfileClient(@Value("${clients.profile-base-url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public BasicsResponse getBasics(String authorizationHeader, String userId) {
        return rest.get()
                .uri("/profile/{userId}/basics", userId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(BasicsResponse.class);
    }

    public record BasicsResponse(String userId, String gender) {}
}