package com.gary.assistant.controller;

import com.gary.assistant.service.ScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    private HealthController healthController;

    @Mock
    private ScraperService scraperService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        healthController = new HealthController(scraperService);
    }

    @Test
    void health_ShouldReturnOk() {
        ResponseEntity<Map<String, Object>> response = healthController.health();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("Gary Assistant API", body.get("service"));
        assertNotNull(body.get("timestamp"));
    }
}
