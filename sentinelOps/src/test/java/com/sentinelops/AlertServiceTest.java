package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.model.Alert;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.repository.AlertRepository;
import com.sentinelops.service.AlertCooldownService;
import com.sentinelops.service.AlertService;
import com.sentinelops.service.IncidentService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.sentinelops.dto.AlertResponse;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.exception.ResourceNotFoundException;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.Severity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    @Mock
    private AlertCooldownService cooldownService;

    @Mock
    private IncidentService incidentService;

    @InjectMocks
    private AlertService alertService;

    private DetectionResult result;
    private SecurityEvent event;

    @BeforeEach
    void setUp() {

        result = new DetectionResult(
        			true,
                "BRUTE_FORCE",
                com.sentinelops.model.Severity.HIGH,
                "Multiple failed login attempts",
                "192.168.0.10"
        );

        event = new SecurityEvent();
    }

    @Test
    void shouldCreateAlertWhenCooldownIsAvailable() {

        when(cooldownService.startCoolDown(
                "sentinelops:alert:BRUTE_FORCE:192.168.0.10",
                300
        )).thenReturn(true);

        when(repository.save(any(Alert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        System.out.println(
                "DetectionResult severity: " + result.severity()
        );

        Alert resultAlert =
                alertService.createAlert(result, event);

        assertNotNull(resultAlert);

        assertEquals(
                Severity.HIGH,
                resultAlert.getSeverity()
        );

        assertEquals(
                "BRUTE_FORCE",
                resultAlert.getRule()
        );

        assertEquals(
                "Multiple failed login attempts",
                resultAlert.getDescription()
        );

        assertEquals(
                AlertStatus.OPEN,
                resultAlert.getStatus()
        );

        assertEquals(
                event,
                resultAlert.getSecurityEvent()
        );

        verify(repository).save(any(Alert.class));

        verify(incidentService)
                .processAlert(any(Alert.class));
    }
    
    @Test
    void shouldNotCreateAlertWhenCooldownIsActive() {

        when(cooldownService.startCoolDown(
                "sentinelops:alert:BRUTE_FORCE:192.168.0.10",
                300
        )).thenReturn(false);

        Alert resultAlert = alertService.createAlert(result, event);

        assertNull(resultAlert);

        verify(repository, never())
                .save(any(Alert.class));

        verify(incidentService, never())
                .processAlert(any(Alert.class));
    }
    
    @Test
    void shouldFindAlertById() {

        Alert alert = new Alert();

        alert.setId(1L);
        alert.setCreatedAt(Instant.now());
        alert.setSeverity(Severity.HIGH);
        alert.setRule("BRUTE_FORCE");
        alert.setDescription("Multiple failed login attempts");
        alert.setStatus(AlertStatus.OPEN);

        when(repository.findById(1L))
                .thenReturn(Optional.of(alert));

        AlertResponse response =
        		alertService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals(
                "BRUTE_FORCE",
                response.rule()
        );
        assertEquals(
                Severity.HIGH,
                response.severity()
        );
        assertEquals(
                AlertStatus.OPEN,
                response.status()
        );

        verify(repository).findById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenAlertDoesNotExist() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.findById(999L)
        );

        verify(repository).findById(999L);
    }
    
    @Test
    void shouldFindAllAlerts() {

        Alert alert = new Alert();

        alert.setId(1L);
        alert.setCreatedAt(Instant.now());
        alert.setSeverity(Severity.HIGH);
        alert.setRule("BRUTE_FORCE");
        alert.setDescription("Multiple failed login attempts");
        alert.setStatus(AlertStatus.OPEN);

        Page<Alert> page =
                new PageImpl<>(List.of(alert));

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        when(repository.findAll(pageable))
                .thenReturn(page);

        Page<AlertResponse> response =
        		alertService.findAll(
                        null,
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                "BRUTE_FORCE",
                response.getContent()
                        .get(0)
                        .rule()
        );

        verify(repository).findAll(pageable);
    }
    
    @Test
    void shouldFilterAlertsBySeverity() {

        Alert alert = new Alert();

        alert.setId(1L);
        alert.setCreatedAt(Instant.now());
        alert.setSeverity(Severity.CRITICAL);
        alert.setRule("BRUTE_FORCE");
        alert.setDescription("Critical brute force attack");
        alert.setStatus(AlertStatus.OPEN);

        Page<Alert> page =
                new PageImpl<>(List.of(alert));

        Pageable pageable =
                PageRequest.of(0, 20);

        when(repository.findAll(
                ArgumentMatchers.<Specification<Alert>>any(),
                eq(pageable)
        )).thenReturn(page);

        Page<AlertResponse> response =
        		alertService.findAll(
                        null,
                        Severity.CRITICAL,
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                Severity.CRITICAL,
                response.getContent()
                        .get(0)
                        .severity()
        );

        verify(repository).findAll(
                ArgumentMatchers.<Specification<Alert>>any(),
                eq(pageable)
        );
    }
    
    @Test
    void shouldFilterAlertsByRule() {

        Page<Alert> page =
                new PageImpl<>(List.of());

        Pageable pageable =
                PageRequest.of(0, 20);

        when(repository.findAll(
                ArgumentMatchers.<Specification<Alert>>any(),
                eq(pageable)
        )).thenReturn(page);

        Page<AlertResponse> response =
        		alertService.findAll(
                        null,
                        null,
                        "  BRUTE_FORCE  ",
                        null,
                        null,
                        pageable
                );

        assertEquals(
                0,
                response.getTotalElements()
        );

        verify(repository).findAll(
                ArgumentMatchers.<Specification<Alert>>any(),
                eq(pageable)
        );
    }
    
    @Test
    void shouldRejectInvalidDateRange() {

        Instant from =
                Instant.parse("2026-09-20T00:00:00Z");

        Instant to =
                Instant.parse("2026-09-19T00:00:00Z");

        Pageable pageable =
                PageRequest.of(0, 20);

        assertThrows(
                InvalidSecurityEventException.class,
                () -> alertService.findAll(
                        null,
                        null,
                        null,
                        from,
                        to,
                        pageable
                )
        );

        verifyNoInteractions(repository);
    }
    
    
    
}