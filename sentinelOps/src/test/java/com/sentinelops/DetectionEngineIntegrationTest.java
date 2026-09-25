package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentinelops.detection.DetectionEngine;
import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.rules.BruteForceRule;
import com.sentinelops.detection.rules.SuspiciousStatusCodeRule;
import com.sentinelops.model.SecurityEvent;

class DetectionEngineIntegrationTest {

    private DetectionEngine engine;

    @BeforeEach
    void setUp() {

        engine = new DetectionEngine(
                List.of(
                        new BruteForceRule(),
                        new SuspiciousStatusCodeRule()
                )
        );
    }

    private SecurityEvent createEvent(
            String sourceip,
            Integer statuscode,
            Instant timestamp) {

        SecurityEvent event = new SecurityEvent();

        event.setSourceip(sourceip);
        event.setStatuscode(statuscode);
        event.setTimestamp(timestamp);

        return event;
    }

    @Test
    void shouldDetectBruteForceAndUnauthorizedRequest() {

        Instant base = Instant.parse("2026-09-25T10:00:00Z");

        List<SecurityEvent> events = List.of(
                createEvent("192.168.0.10", 401, base),
                createEvent("192.168.0.10", 401, base.plusSeconds(60)),
                createEvent("192.168.0.10", 401, base.plusSeconds(120)),
                createEvent("192.168.0.10", 401, base.plusSeconds(180)),
                createEvent("192.168.0.10", 401, base.plusSeconds(240))
        );

        List<DetectionResult> results = engine.analyze(events);

        assertEquals(2, results.size());

        assertTrue(results.stream()
                .anyMatch(result ->
                        result.rule().equals("BruteForceRule")));

        assertTrue(results.stream()
                .anyMatch(result ->
                        result.rule().equals("SuspiciousStatusCodeRule")));
    }

    @Test
    void shouldDetectOnlySuspiciousStatusCode() {

        Instant timestamp =
                Instant.parse("2026-09-25T10:00:00Z");

        List<SecurityEvent> events = List.of(
                createEvent("192.168.0.10", 401, timestamp),
                createEvent("192.168.0.20", 200, timestamp),
                createEvent("192.168.0.30", 404, timestamp)
        );

        List<DetectionResult> results = engine.analyze(events);

        assertEquals(1, results.size());

        assertEquals(
                "SuspiciousStatusCodeRule",
                results.get(0).rule()
        );
    }

    @Test
    void shouldDetectOnlyBruteForce() {

        Instant base =
                Instant.parse("2026-09-25T10:00:00Z");

        List<SecurityEvent> events = List.of(
                createEvent(
                        "192.168.0.10",
                        401,
                        base
                ),
                createEvent(
                        "192.168.0.10",
                        401,
                        base.plusSeconds(60)
                ),
                createEvent(
                        "192.168.0.10",
                        401,
                        base.plusSeconds(120)
                ),
                createEvent(
                        "192.168.0.10",
                        401,
                        base.plusSeconds(180)
                ),
                createEvent(
                        "192.168.0.10",
                        401,
                        base.plusSeconds(240)
                )
        );

        // Para este cenário, a regra de 401 também detectará.
        // Portanto, esperamos as duas regras.
        List<DetectionResult> results = engine.analyze(events);

        assertEquals(2, results.size());
    }

    @Test
    void shouldReturnEmptyWhenThereAreNoThreats() {

        Instant timestamp =
                Instant.parse("2026-09-25T10:00:00Z");

        List<SecurityEvent> events = List.of(
                createEvent("192.168.0.10", 200, timestamp),
                createEvent("192.168.0.20", 200, timestamp),
                createEvent("192.168.0.30", 404, timestamp)
        );

        List<DetectionResult> results = engine.analyze(events);

        assertTrue(results.isEmpty());
    }
}