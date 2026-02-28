package com.rmmr.dating.discovery.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InteractionClient {

    private final RestClient rest;

    public InteractionClient(@Value("${clients.interaction-base-url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<String> myTargets(String authorizationHeader) {
        TargetsResponse resp = rest.get()
                .uri("/swipes/targets")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(TargetsResponse.class);

        if (resp == null || resp.targets == null) return List.of();
        return resp.targets;
    }

    public static class TargetsResponse {
        public String fromUserId;
        public List<String> targets;
    }
}