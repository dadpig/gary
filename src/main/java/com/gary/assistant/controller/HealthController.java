package com.gary.assistant.controller;

import com.gary.assistant.model.Platform;
import com.gary.assistant.service.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    private final ScraperService scraperService;

    public HealthController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping
    @Operation(
        summary = "Health check",
        description = "Check if the API is running"
    )
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", Instant.now(),
            "service", "Gary Assistant API"
        ));
    }

    @GetMapping("/scrapers")
    @Operation(
        summary = "Scraper status",
        description = "Check the status of all platform scrapers"
    )
    public ResponseEntity<Map<Platform, Boolean>> scraperStatus() {
        return ResponseEntity.ok(scraperService.getScraperStatus());
    }
}
