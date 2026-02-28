package com.rmmr.dating.messaging.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/messages/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "service", "messaging-service");
    }
}