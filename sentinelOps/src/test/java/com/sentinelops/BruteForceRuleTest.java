package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.rules.BruteForceRule;
import com.sentinelops.model.SecurityEvent;

class BruteForceRuleTest {

    private BruteForceRule rule;

    @BeforeEach
    void setUp() {
        rule = new BruteForceRule();
    }

    @Test
    void shouldNotDetectBruteForceBelowThreshold() {

        List<SecurityEvent> events = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            events.add(createEvent(
                    "192.168.0.10",
                    401,
                    Instant.parse(
                            "2026-09-25T10:0" + i + ":00Z"
                    )
            ));
        }

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("BruteForceRule", result.rule());
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
    void shouldDetectBruteForceAtThreshold() {

        List<SecurityEvent> events = new ArrayList<>();

        for (int i = 0; i < 5; i++) {

            events.add(createEvent(
                    "192.168.0.10",
                    401,
                    Instant.parse(
                            "2026-09-25T10:0" + i + ":00Z"
                    )
            ));
        }

        DetectionResult result = rule.evaluate(events);

        assertTrue(result.detected());
        assertEquals("BruteForceRule", result.rule());
    }
    
    @Test
    void shouldNotDetectBruteForceWhenAttemptsComeFromDifferentIps() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:01:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:02:00Z")
        ));

        events.add(createEvent(
                "192.168.0.20",
                401,
                Instant.parse("2026-09-25T10:03:00Z")
        ));

        events.add(createEvent(
                "192.168.0.20",
                401,
                Instant.parse("2026-09-25T10:04:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("BruteForceRule", result.rule());
    }
    
    @Test
    void shouldNotDetectBruteForceWhenAttemptsAreOutsideTimeWindow() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:10:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:20:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:30:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:40:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("BruteForceRule", result.rule());
    }
    
    @Test
    void shouldDetectBruteForceAtExactTimeWindowBoundary() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:01:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:02:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:03:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:05:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertTrue(result.detected());
        assertEquals("BruteForceRule", result.rule());
    }
    
    @Test
    void shouldNotDetectBruteForceWhenAttemptsAreSpreadOverTime() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:10:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:20:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:30:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:40:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("BruteForceRule", result.rule());
    }
    
    @Test
    void shouldIgnoreNonUnauthorizedStatusCodes() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                200,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                404,
                Instant.parse("2026-09-25T10:01:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                500,
                Instant.parse("2026-09-25T10:02:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                403,
                Instant.parse("2026-09-25T10:03:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("BruteForceRule", result.rule());
    }
    
    @Test
    void shouldCountOnlyUnauthorizedStatusCodes() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                200,
                Instant.parse("2026-09-25T10:01:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:02:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                403,
                Instant.parse("2026-09-25T10:03:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:04:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
    }
    
    @Test
    void shouldIgnoreNullEvents() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(null);

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:01:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
    }
}