package com.rmmr.dating.match.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmmr.dating.match.api.dto.ActivateMatchResponse;
import com.rmmr.dating.match.api.dto.AttemptMatchResponse;
import com.rmmr.dating.match.api.dto.MatchSummaryDto;
import com.rmmr.dating.match.domain.Match;
import com.rmmr.dating.match.domain.MatchRepository;
import com.rmmr.dating.match.domain.MatchState;
import com.rmmr.dating.match.domain.ex.*;
import com.rmmr.dating.match.integration.InteractionClient;
import com.rmmr.dating.match.outbox.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rmmr.dating.match.integration.ProfileClient;

import java.util.UUID;
import java.time.OffsetDateTime;

@Service
public class MatchAppService {

    private final MatchRepository repo;
    private final InteractionClient interactionClient;
    private final int expiryMinutes;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ProfileClient profileClient;

    public MatchAppService(
            MatchRepository repo,
            InteractionClient interactionClient,
            ProfileClient profileClient,
            @Value("${match.expiry-minutes:1440}") int expiryMinutes,
            OutboxRepository outboxRepository, ObjectMapper objectMapper
    ) {
        this.repo = repo;
        this.interactionClient = interactionClient;
        this.profileClient = profileClient;
        this.expiryMinutes = expiryMinutes;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AttemptMatchResponse attempt(String authorizationHeader, String me, String otherUserId) {
        if (me.equals(otherUserId)) {
            return new AttemptMatchResponse(false, null, null, null, null);
        }

        // Orden canónico para evitar duplicados (A,B) vs (B,A)
        String u1 = me.compareTo(otherUserId) <= 0 ? me : otherUserId;
        String u2 = me.compareTo(otherUserId) <= 0 ? otherUserId : me;

        var existing = repo.findByUser1IdAndUser2Id(u1, u2);
        if (existing.isPresent()) {
            var m = existing.get();
            return new AttemptMatchResponse(true, m.getId(), m.getState().name(), m.getExpiresAt(), m.getFirstMoverUserId());
        }

        boolean likedMe = interactionClient.otherUserLikedMe(authorizationHeader, otherUserId);
        if (!likedMe) {
            return new AttemptMatchResponse(false, null, null, null, null);
        }

        var match = new Match();
        match.setUser1Id(u1);
        match.setUser2Id(u2);
        match.setState(MatchState.PENDING_FIRST_MESSAGE);
        match.setExpiresAt(OffsetDateTime.now().plusMinutes(expiryMinutes));

        // Aqui decidimos quien inicia --> En este caso las mujeres
        String firstMover = computeFirstMover(authorizationHeader, u1, u2);
        match.setFirstMoverUserId(firstMover);

        try {
            var saved = repo.save(match);

            enqueueEvent(
                    "MatchCreated",
                    "Match",
                    saved.getId().toString(),
                    java.util.Map.of(
                            "matchId", saved.getId().toString(),
                            "user1Id", saved.getUser1Id(),
                            "user2Id", saved.getUser2Id(),
                            "state", saved.getState().name(),
                            "firstMoverUserId", saved.getFirstMoverUserId(),
                            "expiresAt", saved.getExpiresAt() != null ? saved.getExpiresAt().toString() : null
                    )
            );

            return new AttemptMatchResponse(true, saved.getId(), saved.getState().name(), saved.getExpiresAt(), saved.getFirstMoverUserId());
        } catch (DataIntegrityViolationException dup) {
            // si dos requests compiten, caemos aquí; regresamos el existente
            var m = repo.findByUser1IdAndUser2Id(u1, u2).orElseThrow();
            return new AttemptMatchResponse(true, m.getId(), m.getState().name(), m.getExpiresAt(), m.getFirstMoverUserId());
        }
    }

    public int expireDueMatches() {
        var now = OffsetDateTime.now();
        var due = repo.findByStateAndExpiresAtBefore(MatchState.PENDING_FIRST_MESSAGE, now);

        for (var m : due) {
            m.setState(MatchState.EXPIRED);
            m.setExpiredAt(now);
        }

        repo.saveAll(due);
        return due.size();
    }

    @Transactional
    public ActivateMatchResponse activate(UUID matchId, String userId) {
        var m = repo.findById(matchId)
                //.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "match_not_found"));
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        // Solo participantes
        boolean isParticipant = userId.equals(m.getUser1Id()) || userId.equals(m.getUser2Id());
        if (!isParticipant) {
            //throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not_a_participant");
            throw new NotParticipantException(matchId);
        }

        // Si ya activo, idempotente
        if (m.getState() == MatchState.ACTIVE) {
            return new ActivateMatchResponse(m.getId(), "ACTIVE", "ACTIVE", m.getActivatedAt());
        }

        // Si ya expiró
        if (m.getState() == MatchState.EXPIRED) {
            //throw new ResponseStatusException(HttpStatus.CONFLICT, "match_expired");
            throw new MatchExpiredException(matchId);
        }

        // Expiración por tiempo (por si el job aún no corrió)
        if (m.getExpiresAt() != null && m.getExpiresAt().isBefore(OffsetDateTime.now())) {
            m.setState(MatchState.EXPIRED);
            m.setExpiredAt(OffsetDateTime.now());
            repo.save(m);
            //throw new ResponseStatusException(HttpStatus.CONFLICT, "match_expired");
            throw new MatchExpiredException(matchId);
        }

        // Regla first-mover (por ahora null = cualquiera)
        if (m.getFirstMoverUserId() != null && !m.getFirstMoverUserId().equals(userId)) {
            //throw new ResponseStatusException(HttpStatus.CONFLICT, "first_mover_only");
            throw new FirstMoverOnlyException(matchId);
        }

        if (m.getState() != MatchState.PENDING_FIRST_MESSAGE) {
            //throw new ResponseStatusException(HttpStatus.CONFLICT, "invalid_state");
            throw new InvalidMatchStateException(matchId);
        }

        String prev = m.getState().name();
        m.setState(MatchState.ACTIVE);

        var now = OffsetDateTime.now();
        m.setActivatedAt(now);

        repo.save(m);

        enqueueEvent(
                "MatchActivated",
                "Match",
                m.getId().toString(),
                java.util.Map.of(
                        "matchId", m.getId().toString(),
                        "previousState", prev,
                        "state", m.getState().name(),
                        "activatedAt", now.toString()
                )
        );

        return new ActivateMatchResponse(m.getId(), prev, m.getState().name(), m.getActivatedAt());    }

    public MatchSummaryDto getMatchForUser(UUID matchId, String userId) {
        var m = repo.findById(matchId)
                //.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "match_not_found"));
                .orElseThrow(()-> new MatchNotFoundException(matchId));

        boolean isParticipant = userId.equals(m.getUser1Id()) || userId.equals(m.getUser2Id());
        if (!isParticipant) {
            //throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not_a_participant");
            throw new NotParticipantException(matchId);
        }

        return new MatchSummaryDto(
                m.getId(),
                m.getUser1Id(),
                m.getUser2Id(),
                m.getState().name(),
                m.getFirstMoverUserId(),
                m.getCreatedAt(),
                m.getExpiresAt()
        );
    }

    private String computeFirstMover(String authorizationHeader, String userA, String userB) {
        var a = profileClient.getBasics(authorizationHeader, userA);
        var b = profileClient.getBasics(authorizationHeader, userB);

        if (a == null || b == null) return null;

        String ga = a.gender();
        String gb = b.gender();

        boolean aFemale = "FEMALE".equalsIgnoreCase(ga);
        boolean bFemale = "FEMALE".equalsIgnoreCase(gb);
        boolean aMale = "MALE".equalsIgnoreCase(ga);
        boolean bMale = "MALE".equalsIgnoreCase(gb);

        // hetero (M/F): mujer inicia
        if ((aFemale && bMale) || (aMale && bFemale)) {
            return aFemale ? userA : userB;
        }

        // same-sex o non-binary/unknown: cualquiera
        return null;
    }

    private void enqueueEvent(String eventType, String aggregateType, String aggregateId, Object payloadObj) {
        try {
            var e = new com.rmmr.dating.match.outbox.OutboxEvent();
            e.setAggregateType(aggregateType);
            e.setAggregateId(aggregateId);
            e.setEventType(eventType);
            e.setAttempts(0);
            e.setPayload(objectMapper.writeValueAsString(payloadObj));
            outboxRepository.save(e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to enqueue outbox event", ex);
        }
    }
}


