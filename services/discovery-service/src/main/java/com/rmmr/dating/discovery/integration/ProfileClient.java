package com.rmmr.dating.discovery.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProfileClient {

    private final RestClient rest;

    public ProfileClient(@Value("${clients.profile-base-url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<Basics> listBasics(String authorizationHeader) {
        Basics[] arr = rest.get()
                .uri("/profile/basics")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(Basics[].class);
        return arr == null ? List.of() : List.of(arr);
    }

    public record Basics(String userId, String gender, String showMe) {}
}