package com.sentinelops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.repository.AlertRepository;
import com.sentinelops.repository.SecurityEventRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityEventIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    void shouldReceiveEventAndPersistIt() {

        SecurityEventRequest request = new SecurityEventRequest(
                Instant.now(),
                "192.168.1.10",
                "GET",
                "/login",
                200,
                "apache"
        );

        String url = "http://localhost:" + port + "/api/v1/events";

        ResponseEntity<SecurityEvent> response =
                restTemplate.postForEntity(
                        url,
                        new HttpEntity<>(request),
                        SecurityEvent.class
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());

        List<SecurityEvent> events =
                securityEventRepository.findAll();

        assertEquals(1, events.size());
    }

    @Test
    void shouldDetectBruteForceAndCreateAlert() {

        String ip = "10.10.10.50";

        String url = "http://localhost:" + port + "/api/v1/events";

        for (int i = 0; i < 5; i++) {

            SecurityEventRequest request = new SecurityEventRequest(
                    Instant.now(),
                    ip,
                    "POST",
                    "/login",
                    401,
                    "apache"
            );

            restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    SecurityEvent.class
            );
        }

        List<Alert> alerts = alertRepository.findAll();

        assertNotNull(alerts);
        assertEquals(1, alerts.size());

        Alert alert = alerts.get(0);

        assertEquals("BruteForceRule", alert.getRule());
        assertEquals(AlertStatus.OPEN, alert.getStatus());
    }
}
