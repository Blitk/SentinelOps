package com.sentinelops.detection.rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

class BruteForceRuleTest {

    private final BruteForceRule rule = new BruteForceRule();

    @Test
    void shouldDetectBruteForceFromSameIp() {

        List<SecurityEvent> events = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            SecurityEvent event = new SecurityEvent();

            event.setSourceip("192.168.1.50");
            event.setStatuscode(401);

            events.add(event);
        }

        DetectionResult result = rule.evaluate(events);

        assertTrue(result.detected());
        assertEquals("BruteForceRule", result.rule());
        assertEquals(Severity.HIGH, result.severity());
    }

    @Test
    void shouldNotDetectDifferentIps() {

        List<SecurityEvent> events = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            SecurityEvent event = new SecurityEvent();

            event.setSourceip("192.168.1." + (10 + i));
            event.setStatuscode(401);

            events.add(event);
        }

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
    }

    @Test
    void shouldNotDetectLessThanThreshold() {

        List<SecurityEvent> events = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            SecurityEvent event = new SecurityEvent();

            event.setSourceip("192.168.1.50");
            event.setStatuscode(401);

            events.add(event);
        }

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
    }

    @Test
    void shouldIgnoreSuccessfulRequests() {

        List<SecurityEvent> events = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            SecurityEvent event = new SecurityEvent();

            event.setSourceip("192.168.1.50");
            event.setStatuscode(200);

            events.add(event);
        }

        DetectionResult result = rule.evaluate(events);

        assertFalse(result.detected());
    }
}
