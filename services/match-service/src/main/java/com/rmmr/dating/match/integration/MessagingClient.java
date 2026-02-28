package com.rmmr.dating.match.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.rmmr.dating.match.integration.dto.LastMessagesBatchRequest;
import com.rmmr.dating.match.integration.dto.LastMessagesBatchResponse;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class MessagingClient {

    private final RestClient rest;

    public MessagingClient(@Value("${clients.messaging-base-url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Optional<LastMessageDto> lastMessage(String authorizationHeader, UUID matchId) {
        try {
            var resp = rest.get()
                    .uri("/matches/{id}/messages/last", matchId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .toEntity(LastMessageDto.class);

            // 204 -> body null
            return Optional.ofNullable(resp.getBody());
        } catch (RestClientResponseException ex) {
            // MVP: si falla, no tumbar inbox completo
            return Optional.empty();
        }
    }

    public record LastMessageDto(
            UUID id,
            UUID matchId,
            String senderUserId,
            String content,
            OffsetDateTime createdAt
    ) {}

    public LastMessagesBatchResponse lastMessagesBatch(String authorizationHeader, java.util.List<java.util.UUID> matchIds) {
        return rest.post()
                .uri("/internal/messages/last")
                .header(org.springframework.http.HttpHeaders.AUTHORIZATION, authorizationHeader)
                .body(new LastMessagesBatchRequest(matchIds))
                .retrieve()
                .body(LastMessagesBatchResponse.class);
    }
}