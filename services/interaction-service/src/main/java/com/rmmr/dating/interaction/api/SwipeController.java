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

    public record SwipeRequest(String toUserId, String action) {}

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
            return new MatchAttemptResult(false, java.util.Map.of()); // <- NO null
        }

        try {
            var resp = matchClient.attemptMatch(authorization, req.toUserId());
            if (resp == null) {
                return new MatchAttemptResult(false, java.util.Map.of()); // <- NO null
            }
            return new MatchAttemptResult(resp.matched(), resp);
        } catch (Exception ex) {
            return new MatchAttemptResult(false, java.util.Map.of("error", "match-service-unavailable"));
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

    @GetMapping("/targets")
    public Map<String, Object> targets(@AuthenticationPrincipal Jwt jwt) {
        String me = jwt.getSubject();
        var ids = swipeRepository.findAllToUserIdsByFromUserId(me);
        return Map.of("fromUserId", me, "targets", ids);
    }

}