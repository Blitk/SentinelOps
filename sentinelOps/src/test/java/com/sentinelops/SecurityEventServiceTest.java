package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentinelops.detection.DetectionEngine;
import com.sentinelops.detection.DetectionResult;
import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;
import com.sentinelops.repository.SecurityEventRepository;
import com.sentinelops.service.AlertService;
import com.sentinelops.service.SecurityEventService;

@ExtendWith(MockitoExtension.class)
class SecurityEventServiceTest {

    @Mock
    private SecurityEventRepository repository;

    @Mock
    private DetectionEngine detectionEngine;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private SecurityEventService service;

    private SecurityEventRequest request;

    @BeforeEach
    void setUp() {

        request = new SecurityEventRequest(
                Instant.parse("2026-09-21T12:00:00Z"),
                "192.168.0.10",
                "get",
                "/login",
                401,
                "apache"
        );
    }


    @Test
    void shouldReceiveAndSaveSecurityEvent() {

        SecurityEvent savedEvent = new SecurityEvent();

        savedEvent.setId(1L);
        savedEvent.setTimestamp(request.timestamp());
        savedEvent.setSourceip("192.168.0.10");
        savedEvent.setMethod("GET");
        savedEvent.setPath("/login");
        savedEvent.setStatuscode(401);
        savedEvent.setSource("APACHE");
        savedEvent.setReceivedAt(Instant.now());

        when(repository.save(any(SecurityEvent.class)))
                .thenReturn(savedEvent);

        when(repository.findTop100ByOrderByTimestampDesc())
                .thenReturn(List.of(savedEvent));

        when(detectionEngine.analyze(anyList()))
                .thenReturn(List.of());

        SecurityEvent result =
                service.receiveEvent(request);

        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "192.168.0.10",
                result.getSourceip()
        );

        assertEquals(
                "GET",
                result.getMethod()
        );

        assertEquals(
                "/login",
                result.getPath()
        );

        assertEquals(
                401,
                result.getStatuscode()
        );

        assertEquals(
                "APACHE",
                result.getSource()
        );

        verify(repository)
                .save(any(SecurityEvent.class));

        verify(repository)
                .findTop100ByOrderByTimestampDesc();

        verify(detectionEngine)
                .analyze(anyList());

        verifyNoInteractions(alertService);
    }
    
    @Test
    void shouldCreateAlertWhenDetectionIsFound() {

        SecurityEvent savedEvent = new SecurityEvent();

        savedEvent.setId(1L);
        savedEvent.setTimestamp(request.timestamp());
        savedEvent.setSourceip("192.168.0.10");
        savedEvent.setMethod("GET");
        savedEvent.setPath("/login");
        savedEvent.setStatuscode(401);
        savedEvent.setSource("APACHE");
        savedEvent.setReceivedAt(Instant.now());

        DetectionResult detectionResult =
                new DetectionResult(
                        true,
                        "BRUTE_FORCE",
                        Severity.HIGH,
                        "Multiple failed login attempts",
                        "192.168.0.10"
                );

        when(repository.save(any(SecurityEvent.class)))
                .thenReturn(savedEvent);

        when(repository.findTop100ByOrderByTimestampDesc())
                .thenReturn(List.of(savedEvent));

        when(detectionEngine.analyze(anyList()))
                .thenReturn(List.of(detectionResult));

        service.receiveEvent(request);

        verify(alertService)
                .createAlert(
                        detectionResult,
                        savedEvent
                );
    }
    
    @Test
    void shouldNormalizeSecurityEvent() {

        SecurityEvent event = service.normalize(request);

        assertEquals(
                Instant.parse("2026-09-22T18:00:00Z"),
                event.getTimestamp()
        );

        assertEquals(
                "192.168.0.10",
                event.getSourceip()
        );

        assertEquals(
                "POST",
                event.getMethod()
        );

        assertEquals(
                "/login",
                event.getPath()
        );

        assertEquals(
                401,
                event.getStatuscode()
        );

        assertEquals(
                "APACHE",
                event.getSource()
        );

        assertNotNull(event.getReceivedAt());
    }
    
    @Test
    void shouldRejectNullRequest() {

        assertThrows(
                InvalidSecurityEventException.class,
                () -> service.receiveEvent(null)
        );

        verifyNoInteractions(repository);
        verifyNoInteractions(detectionEngine);
        verifyNoInteractions(alertService);
    }
    
    @Test
    void shouldRejectInvalidStatusCode() {

        SecurityEventRequest invalidRequest =
                new SecurityEventRequest(
                        Instant.now(),
                        "192.168.0.10",
                        "GET",
                        "/login",
                        700,
                        "apache"
                );

        assertThrows(
                InvalidSecurityEventException.class,
                () -> service.receiveEvent(invalidRequest)
        );

        verifyNoInteractions(repository);
        verifyNoInteractions(detectionEngine);
        verifyNoInteractions(alertService);
    }
    
    @Test
    void shouldCreateAlertsForMultipleDetections() {

        SecurityEvent savedEvent = new SecurityEvent();

        savedEvent.setId(1L);

        DetectionResult bruteForce =
                new DetectionResult(
                        true,
                        "BRUTE_FORCE",
                        Severity.HIGH,
                        "Multiple failed login attempts",
                        "192.168.0.10"
                );

        DetectionResult suspiciousPath =
                new DetectionResult(
                        true,
                        "SUSPICIOUS_PATH",
                        Severity.MEDIUM,
                        "Suspicious path detected",
                        "192.168.0.10"
                );

        when(repository.save(any(SecurityEvent.class)))
                .thenReturn(savedEvent);

        when(repository.findTop100ByOrderByTimestampDesc())
                .thenReturn(List.of(savedEvent));

        when(detectionEngine.analyze(anyList()))
                .thenReturn(List.of(
                        bruteForce,
                        suspiciousPath
                ));

        service.receiveEvent(request);

        verify(alertService)
                .createAlert(bruteForce, savedEvent);

        verify(alertService)
                .createAlert(suspiciousPath, savedEvent);

        verify(alertService, times(2))
                .createAlert(any(DetectionResult.class), eq(savedEvent));
    }
    
    
    
}