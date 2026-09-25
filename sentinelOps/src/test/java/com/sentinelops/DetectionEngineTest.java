package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentinelops.detection.DetectionEngine;
import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.DetectionRule;
import com.sentinelops.model.SecurityEvent;

class DetectionEngineTest {

    private DetectionRule rule1;
    private DetectionRule rule2;
    private DetectionRule rule3;

    private DetectionEngine engine;

    @BeforeEach
    void setUp() {

        rule1 = mock(DetectionRule.class);
        rule2 = mock(DetectionRule.class);
        rule3 = mock(DetectionRule.class);

        engine = new DetectionEngine(
                List.of(rule1, rule2, rule3)
        );
    }

    @Test
    void shouldReturnOnlyDetectedResults() {

        SecurityEvent event = new SecurityEvent();

        DetectionResult detected = DetectionResult.detected(
                "Rule1",
                com.sentinelops.model.Severity.HIGH,
                "Threat detected",
                "192.168.0.10"
        );

        DetectionResult notDetected = DetectionResult.notDetected("Rule2");

        DetectionResult detected2 = DetectionResult.detected(
                "Rule3",
                com.sentinelops.model.Severity.MEDIUM,
                "Suspicious activity",
                "192.168.0.20"
        );

        when(rule1.evaluate(anyList())).thenReturn(detected);
        when(rule2.evaluate(anyList())).thenReturn(notDetected);
        when(rule3.evaluate(anyList())).thenReturn(detected2);

        List<DetectionResult> results = engine.analyze(
                List.of(event)
        );

        assertEquals(2, results.size());

        assertTrue(results.contains(detected));
        assertTrue(results.contains(detected2));
        assertFalse(results.contains(notDetected));
    }

    @Test
    void shouldExecuteAllDetectionRules() {

        List<SecurityEvent> events = List.of(
                new SecurityEvent()
        );

        when(rule1.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule1"));

        when(rule2.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule2"));

        when(rule3.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule3"));

        engine.analyze(events);

        verify(rule1, times(1)).evaluate(events);
        verify(rule2, times(1)).evaluate(events);
        verify(rule3, times(1)).evaluate(events);
    }

    @Test
    void shouldReturnEmptyListWhenNoRuleDetects() {

        when(rule1.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule1"));

        when(rule2.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule2"));

        when(rule3.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule3"));

        List<DetectionResult> results = engine.analyze(
                List.of(new SecurityEvent())
        );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldAnalyzeEmptyEventsList() {

        when(rule1.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule1"));

        when(rule2.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule2"));

        when(rule3.evaluate(anyList()))
                .thenReturn(DetectionResult.notDetected("Rule3"));

        List<DetectionResult> results = engine.analyze(
                List.of()
        );

        assertTrue(results.isEmpty());

        verify(rule1).evaluate(anyList());
        verify(rule2).evaluate(anyList());
        verify(rule3).evaluate(anyList());
    }
}