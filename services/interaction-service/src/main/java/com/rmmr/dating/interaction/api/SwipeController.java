package com.rmmr.dating.interaction.api;

import com.rmmr.dating.interaction.domain.Swipe;
import com.rmmr.dating.interaction.domain.SwipeRepository;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.rmmr.dating.interaction.integration.MatchClient;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/swipes")
public class SwipeController {

    private final SwipeRepository swipeRepository;

    private final MatchClient matchClient;

    public SwipeController(SwipeRepository swipeRepository, MatchClient matchClient) {
        this.swipeRepository = swipeRepository;
        this.matchClient = matchClient;
    }

    /** este estaba anteriormente
    public SwipeController(SwipeRepository swipeRepository) {
        this.swipeRepository = swipeRepository;
    }**/

    public record SwipeRequest(String toUserId, String action) {}

    /**@PostMapping
    public Map<String, Object> create(@AuthenticationPrincipal Jwt jwt, @RequestBody SwipeRequest req) {
        String fromUserId = jwt.getSubject();

        try {
            Swipe saved = swipeRepository.save(new Swipe(fromUserId, req.toUserId(), req.action()));
            return Map.of("ok", true, "id", saved.getId());
        } catch (DataIntegrityViolationException e) {
            // ya existía (from,to) por unique index
            return Map.of("ok", true, "duplicated", true);
        }
    }**/

    /**
    @PostMapping
    public Map<String, Object> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SwipeRequest req
    ) {
        String fromUserId = jwt.getSubject();

        try {
            Swipe saved = swipeRepository.save(new Swipe(fromUserId, req.toUserId(), req.action()));

            // Paso 9: si fue LIKE, intenta crear match
            Object matchInfo = null;
            boolean matched = false;

            if ("LIKE".equalsIgnoreCase(req.action())) {
                try {
                    var resp = matchClient.attemptMatch(authorization, req.toUserId());
                    matchInfo = resp;
                    matched = resp != null && resp.matched();
                } catch (Exception ex) {
                    // MVP: si match-service falla, NO tires el swipe
                    matchInfo = Map.of("error", "match-service-unavailable");
                    matched = false;
                }
            }

            return Map.of(
                    "ok", true,
                    "id", saved.getId(),
                    "matched", matched,
                    "match", matchInfo
            );

        } catch (DataIntegrityViolationException e) {
            // ya existía (from,to) por unique index
            return Map.of("ok", true, "duplicated", true);
        }
    }**/

    @PostMapping
    public Map<String, Object> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SwipeRequest req
    ) {
        String fromUserId = jwt.getSubject();

        try {
            Swipe saved = swipeRepository.save(new Swipe(fromUserId, req.toUserId(), req.action()));

            // Intentar match si fue LIKE
            var matchResult = attemptMatchIfLike(authorization, req);

            return Map.of(
                    "ok", true,
                    "id", saved.getId(),
                    "matched", matchResult.matched,
                    "match", matchResult.matchInfo
            );

        } catch (DataIntegrityViolationException e) {
            // Ya existía swipe (from,to). Aun así: si es LIKE, intentamos match (idempotente)
            var matchResult = attemptMatchIfLike(authorization, req);

            return Map.of(
                    "ok", true,
                    "duplicated", true,
                    "matched", matchResult.matched,
                    "match", matchResult.matchInfo
            );
        }
    }

    private MatchAttemptResult attemptMatchIfLike(String authorization, SwipeRequest req) {
        if (!"LIKE".equalsIgnoreCase(req.action())) {
            return new MatchAttemptResult(false, null);
        }

        try {
            var resp = matchClient.attemptMatch(authorization, req.toUserId());
            boolean matched = resp != null && resp.matched();
            return new MatchAttemptResult(matched, resp);
        } catch (Exception ex) {
            // MVP tolerante: no tumbar swipe si match-service falla
            return new MatchAttemptResult(false, Map.of("error", "match-service-unavailable"));
        }
    }

    private record MatchAttemptResult(boolean matched, Object matchInfo) {}

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "service", "interaction-service");
    }

    @GetMapping("/match-check")
    public Map<String, Object> matchCheck(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String otherUserId
    ) {
        String me = jwt.getSubject();
        boolean likedMe = swipeRepository.existsByFromUserIdAndToUserIdAndAction(otherUserId, me, "LIKE");
        return Map.of("otherUserId", otherUserId, "likedMe", likedMe);
    }

}