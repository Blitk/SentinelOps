package com.sentinelops.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

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

    @InjectMocks
    private AlertService alertService;

    @Test
    void shouldCreateAlertFromDetectionResult() {

        DetectionResult result = DetectionResult.detected(
                "BruteForceRule",
                Severity.HIGH,
                "Possible brute-force attack detected."
        );

        SecurityEvent event = new SecurityEvent();

        Alert savedAlert = new Alert();

        when(repository.save(any(Alert.class)))
                .thenReturn(savedAlert);

        Alert resultAlert = alertService.createAlert(result, event);

        assertSame(savedAlert, resultAlert);

        verify(repository).save(any(Alert.class));
    }

    @Test
    void shouldPopulateAlertFieldsCorrectly() {

        DetectionResult result = DetectionResult.detected(
                "SuspiciousStatusCodeRule",
                Severity.MEDIUM,
                "Unauthorized request detected."
        );

        SecurityEvent event = new SecurityEvent();

        when(repository.save(any(Alert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Alert alert = alertService.createAlert(result, event);

        assertEquals("SuspiciousStatusCodeRule", alert.getRule());
        assertEquals(Severity.MEDIUM, alert.getSeverity());
        assertEquals(
                "Unauthorized request detected.",
                alert.getDescription()
        );
        assertEquals(AlertStatus.OPEN, alert.getStatus());
        assertSame(event, alert.getSecurityEvent());
    }
}
