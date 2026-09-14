package com.sentinelops.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;
import com.sentinelops.repository.AlertRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    @Mock
    private AlertCooldownService cooldownService;

    @InjectMocks
    private AlertService alertService;

    @Test
    void shouldCreateAlertWhenCooldownIsNotActive() {

        DetectionResult result = DetectionResult.detected(
                "BruteForceRule",
                Severity.HIGH,
                "Possible brute-force attack detected.",
                "192.168.1.10"
        );

        SecurityEvent event = createEvent(
                "192.168.1.10",
                401
        );

        when(cooldownService.startCooldown(
                "sentinelops:alert:BruteForceRule:192.168.1.10",
                300
        )).thenReturn(true);

        Alert savedAlert = alertService.createAlert(result, event);

        assertNotNull(savedAlert);
        assertEquals(Severity.HIGH, savedAlert.getSeverity());
        assertEquals("BruteForceRule", savedAlert.getRule());
        assertEquals(
                "Possible brute-force attack detected.",
                savedAlert.getDescription()
        );
        assertEquals(AlertStatus.OPEN, savedAlert.getStatus());
        assertEquals(event, savedAlert.getSecurityEvent());

        verify(repository).save(any(Alert.class));
    }

    @Test
    void shouldNotCreateAlertWhenCooldownIsActive() {

        DetectionResult result = DetectionResult.detected(
                "BruteForceRule",
                Severity.HIGH,
                "Possible brute-force attack detected.",
                "192.168.1.10"
        );

        SecurityEvent event = createEvent(
                "192.168.1.10",
                401
        );

        when(cooldownService.startCooldown(
                "sentinelops:alert:BruteForceRule:192.168.1.10",
                300
        )).thenReturn(false);

        Alert alert = alertService.createAlert(result, event);

        assertNull(alert);

        verify(repository, never()).save(any(Alert.class));
    }

    @Test
    void shouldUseGlobalCooldownWhenSourceIpIsNull() {

        DetectionResult result = DetectionResult.detected(
                "SuspiciousStatusCodeRule",
                Severity.MEDIUM,
                "Unauthorized request detected.",
                null
        );

        SecurityEvent event = createEvent(
                null,
                401
        );

        when(cooldownService.startCooldown(
                "sentinelops:alert:SuspiciousStatusCodeRule:global",
                300
        )).thenReturn(true);

        Alert alert = alertService.createAlert(result, event);

        assertNotNull(alert);

        verify(cooldownService).startCooldown(
                "sentinelops:alert:SuspiciousStatusCodeRule:global",
                300
        );

        verify(repository).save(any(Alert.class));
    }

    @Test
    void shouldUseGlobalCooldownWhenSourceIpIsBlank() {

        DetectionResult result = DetectionResult.detected(
                "SuspiciousStatusCodeRule",
                Severity.MEDIUM,
                "Unauthorized request detected.",
                " "
        );

        SecurityEvent event = createEvent(
                " ",
                401
        );

        when(cooldownService.startCooldown(
                "sentinelops:alert:SuspiciousStatusCodeRule:global",
                300
        )).thenReturn(true);

        Alert alert = alertService.createAlert(result, event);

        assertNotNull(alert);

        verify(cooldownService).startCooldown(
                "sentinelops:alert:SuspiciousStatusCodeRule:global",
                300
        );

        verify(repository).save(any(Alert.class));
    }

    private SecurityEvent createEvent(
            String sourceIp,
            int statusCode) {

        SecurityEvent event = new SecurityEvent();

        event.setSourceIp(sourceIp);
        event.setStatusCode(statusCode);

        return event;
    }
}
