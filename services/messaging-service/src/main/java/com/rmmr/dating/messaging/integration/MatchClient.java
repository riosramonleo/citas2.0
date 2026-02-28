package com.rmmr.dating.messaging.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.rmmr.dating.messaging.api.error.UpstreamException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class MatchClient {

    private final RestClient restClient;

    public MatchClient(@Value("${clients.match-base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
    public void activateOrThrow(String authorizationHeader, UUID matchId) {
        restClient.post()
                .uri("/matches/{id}/activate", matchId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .body(new ActivateReq("FIRST_MESSAGE_SENT"))
                .retrieve()
                .toBodilessEntity();
    }**/

    public void activateOrThrow(String authorizationHeader, UUID matchId) {
        try {
            restClient.post()
                    .uri("/matches/{id}/activate", matchId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .body(new ActivateReq("FIRST_MESSAGE_SENT"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

            // Mensajes “humanos” para el cliente
            if (status == HttpStatus.FORBIDDEN) {
                throw new UpstreamException(status, "NOT_A_PARTICIPANT", "No puedes enviar mensajes en este match.");
            }
            if (status == HttpStatus.NOT_FOUND) {
                throw new UpstreamException(status, "MATCH_NOT_FOUND", "Match no encontrado.");
            }
            if (status == HttpStatus.CONFLICT) {
                // aquí caen MATCH_EXPIRED / FIRST_MOVER_ONLY / INVALID_STATE (MVP)
                throw new UpstreamException(status, "MATCH_NOT_AVAILABLE", "No se puede enviar el mensaje: match expirado o no permitido.");
            }

            throw new UpstreamException(HttpStatus.BAD_GATEWAY, "UPSTREAM_ERROR", "No se pudo validar el match.");
        }
    }

    public MatchSummaryDto getMatchOrThrow(String authorizationHeader, UUID matchId) {
        return restClient.get()
                .uri("/matches/{id}", matchId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(MatchSummaryDto.class);
    }

    public record ActivateReq(String reason) {}

    public record MatchSummaryDto(
            UUID id,
            String user1Id,
            String user2Id,
            String state,
            String firstMoverUserId,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime expiresAt
    ) {}
}