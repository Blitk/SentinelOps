package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.rules.SuspiciousStatusCodeRule;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

class SuspiciousStatusCodeRuleTest {

    private SuspiciousStatusCodeRule rule;

    @BeforeEach
    void setUp() {

        rule = new SuspiciousStatusCodeRule();

    }

    @Test
    void shouldDetectUnauthorizedRequest() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                401,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertTrue(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
        assertEquals(Severity.MEDIUM, result.severity());
        assertEquals("192.168.0.10", result.sourceip());
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
    void shouldNotDetectSuccessfulRequest() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                200,
                Instant.parse("2026-09-25T10:00:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
    }
    
    @Test
    void shouldNotDetectWhenEventsListIsEmpty() {

        List<SecurityEvent> events = new ArrayList<>();

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
    }
    
    @Test
    void shouldNotDetectWhenEventsListIsNull() {

        DetectionResult result = rule.evaluate(null);

        assertFalse(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
    }
    
    @Test
    void shouldIgnoreNullEvents() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(null);

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
    }
    
    @Test
    void shouldDetectUnauthorizedRequestAmongOtherEvents() {

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
                401,
                Instant.parse("2026-09-25T10:02:00Z")
        ));

        events.add(createEvent(
                "192.168.0.10",
                500,
                Instant.parse("2026-09-25T10:03:00Z")
        ));

        DetectionResult result = rule.evaluate(events);

        assertTrue(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
        assertEquals(Severity.MEDIUM, result.severity());
        assertEquals("192.168.0.10", result.sourceip());
    }
    
    @Test
    void shouldIgnoreEventsWithNullStatusCode() {

        List<SecurityEvent> events = new ArrayList<>();

        SecurityEvent event = new SecurityEvent();

        event.setSourceip("192.168.0.10");
        event.setStatuscode(null);
        event.setTimestamp(
                Instant.parse("2026-09-25T10:00:00Z")
        );

        events.add(event);

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
        assertEquals("SuspiciousStatusCodeRule", result.rule());
    }
    
    @Test
    void shouldNotDetectOtherStatusCodes() {

        List<SecurityEvent> events = new ArrayList<>();

        events.add(createEvent(
                "192.168.0.10",
                403,
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

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
    }
    
}