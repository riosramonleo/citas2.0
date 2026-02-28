package com.rmmr.dating.discovery.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/discovery/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "service", "discovery-service");
    }
}