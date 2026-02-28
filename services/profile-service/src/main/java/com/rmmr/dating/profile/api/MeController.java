package com.rmmr.dating.profile.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/profile/me")
    public Map<String, Object> me() {
        return Map.of("ok", true, "service", "profile-service");
    }
}