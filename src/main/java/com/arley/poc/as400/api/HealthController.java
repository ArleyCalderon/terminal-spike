package com.arley.poc.as400.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(
            "UP",
            "terminal-spike"
        );
    }

    public record HealthResponse(
        String status,
        String service
    ) {
    }
}