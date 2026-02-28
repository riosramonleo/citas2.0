package com.rmmr.dating.profile.api;

import com.rmmr.dating.profile.api.dto.BasicsResponse;
import com.rmmr.dating.profile.api.dto.UpsertBasicsRequest;
import com.rmmr.dating.profile.domain.ShowMe;
import com.rmmr.dating.profile.domain.UserBasics;
import com.rmmr.dating.profile.domain.UserBasicsRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class BasicsController {

    private final UserBasicsRepository repo;

    public BasicsController(UserBasicsRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/me/basics")
    public BasicsResponse upsertMine(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpsertBasicsRequest req) {
        String me = jwt.getSubject();

        var b = new UserBasics();
        b.setUserId(me);
        b.setGender(req.gender());
        b.setShowMe(req.showMe() != null ? req.showMe() : ShowMe.EVERYONE);

        var saved = repo.save(b);
        return new BasicsResponse(saved.getUserId(), saved.getGender(), saved.getShowMe());
    }

    @GetMapping("/{userId}/basics")
    public BasicsResponse get(@PathVariable String userId) {
        var b = repo.findById(userId).orElseThrow();
        return new BasicsResponse(b.getUserId(), b.getGender(), b.getShowMe());
    }

    // Para discovery-service (MVP)
    @GetMapping("/basics")
    public List<BasicsResponse> listAll() {
        return repo.findAll().stream()
                .map(b -> new BasicsResponse(b.getUserId(), b.getGender(), b.getShowMe()))
                .toList();
    }
}