package com.rmmr.dating.discovery.api;

import com.rmmr.dating.discovery.integration.InteractionClient;
import com.rmmr.dating.discovery.integration.ProfileClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/discovery")
public class DiscoveryController {

    private final ProfileClient profileClient;
    private final InteractionClient interactionClient;

    public DiscoveryController(ProfileClient profileClient, InteractionClient interactionClient) {
        this.profileClient = profileClient;
        this.interactionClient = interactionClient;
    }

    @GetMapping("/feed")
    public List<FeedItem> feed(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "20") int limit
    ) {
        String me = jwt.getSubject();
        var all = profileClient.listBasics(authorization);

        var swipedTargets = new java.util.HashSet<>(interactionClient.myTargets(authorization));

        // find me
        var meBasics = all.stream().filter(b -> me.equals(b.userId())).findFirst().orElse(null);
        String myShowMe = meBasics != null ? meBasics.showMe() : "EVERYONE";

        return all.stream()
                .filter(b -> !me.equals(b.userId()))
                .filter(b -> allowByShowMe(myShowMe, b.gender()))
                .filter(b -> !swipedTargets.contains(b.userId()))
                .limit(limit)
                .map(b -> new FeedItem(b.userId(), b.gender()))
                .toList();
    }

    private boolean allowByShowMe(String showMe, String candidateGender) {
        if (showMe == null) return true;
        if ("EVERYONE".equalsIgnoreCase(showMe)) return true;
        if ("MEN".equalsIgnoreCase(showMe)) return "MALE".equalsIgnoreCase(candidateGender);
        if ("WOMEN".equalsIgnoreCase(showMe)) return "FEMALE".equalsIgnoreCase(candidateGender);
        return true;
    }

    public record FeedItem(String userId, String gender) {}
}