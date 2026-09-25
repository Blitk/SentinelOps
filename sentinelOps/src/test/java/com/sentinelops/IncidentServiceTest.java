package com.sentinelops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.sentinelops.dto.IncidentResponse;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.Incident;
import com.sentinelops.model.IncidentStatus;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;
import com.sentinelops.repository.IncidentRepository;
import com.sentinelops.service.IncidentService;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository repository;

    @InjectMocks
    private IncidentService incidentService;

    private Alert alert;
    private SecurityEvent event;

    @BeforeEach
    void setUp() {

        event = new SecurityEvent();

        event.setSourceip("192.168.0.10");

        alert = new Alert();

        alert.setRule("BRUTE_FORCE");
        alert.setSeverity(Severity.HIGH);
        alert.setStatus(AlertStatus.OPEN);
        alert.setSecurityEvent(event);
    }

    @Test
    void shouldCreateNewIncidentWhenNoOpenIncidentExists() {

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - 192.168.0.10"
        )).thenReturn(Optional.empty());

        when(repository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident result = incidentService.processAlert(alert);

        assertNotNull(result);

        verify(repository).save(any(Incident.class));

        assertEquals(
                1,
                result.getAlerts().size()
        );

        assertEquals(
                alert,
                result.getAlerts().get(0)
        );

        assertEquals(
                result,
                alert.getIncident()
        );
    }

    @Test
    void shouldReuseExistingOpenIncident() {

        Incident existingIncident = new Incident();

        existingIncident.setTitle(
                "BRUTE_FORCE - 192.168.0.10"
        );

        existingIncident.setStatus(
                IncidentStatus.OPEN
        );

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - 192.168.0.10"
        )).thenReturn(Optional.of(existingIncident));

        when(repository.save(any(Incident.class)))
                .thenReturn(existingIncident);

        Incident result = incidentService.processAlert(alert);

        assertNotNull(result);

        assertEquals(
                existingIncident,
                result
        );

        assertEquals(
                1,
                result.getAlerts().size()
        );

        assertEquals(
                alert,
                result.getAlerts().get(0)
        );

        assertEquals(
                existingIncident,
                alert.getIncident()
        );

        verify(repository).save(existingIncident);
    }
    
    @Test
    void shouldAddMultipleAlertsToSameIncident() {

        Incident existingIncident = new Incident();

        existingIncident.setTitle(
                "BRUTE_FORCE - 192.168.0.10"
        );

        existingIncident.setStatus(
                IncidentStatus.OPEN
        );

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - 192.168.0.10"
        )).thenReturn(Optional.of(existingIncident));

        when(repository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident firstResult = incidentService.processAlert(alert);

        Alert secondAlert = new Alert();

        secondAlert.setRule("BRUTE_FORCE");
        secondAlert.setSeverity(Severity.HIGH);
        secondAlert.setStatus(AlertStatus.OPEN);
        secondAlert.setSecurityEvent(event);

        Incident secondResult = incidentService.processAlert(secondAlert);

        assertSame(
                existingIncident,
                firstResult
        );

        assertSame(
                existingIncident,
                secondResult
        );

        assertEquals(
                2,
                existingIncident.getAlerts().size()
        );

        assertTrue(
                existingIncident.getAlerts().contains(alert)
        );

        assertTrue(
                existingIncident.getAlerts().contains(secondAlert)
        );

        assertSame(
                existingIncident,
                alert.getIncident()
        );

        assertSame(
                existingIncident,
                secondAlert.getIncident()
        );

        verify(repository, times(2))
                .save(existingIncident);
    }
    
    @Test
    void shouldCreateDifferentIncidentsForDifferentSourceIps() {

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - 192.168.0.10"
        )).thenReturn(Optional.empty());

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - 192.168.0.20"
        )).thenReturn(Optional.empty());

        when(repository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident firstIncident = incidentService.processAlert(alert);

        SecurityEvent secondEvent = new SecurityEvent();
        secondEvent.setSourceip("192.168.0.20");

        Alert secondAlert = new Alert();

        secondAlert.setRule("BRUTE_FORCE");
        secondAlert.setSeverity(Severity.HIGH);
        secondAlert.setStatus(AlertStatus.OPEN);
        secondAlert.setSecurityEvent(secondEvent);

        Incident secondIncident =
                incidentService.processAlert(secondAlert);

        assertNotNull(firstIncident);
        assertNotNull(secondIncident);

        assertNotSame(
                firstIncident,
                secondIncident
        );

        assertEquals(
                "BRUTE_FORCE - 192.168.0.10",
                firstIncident.getTitle()
        );

        assertEquals(
                "BRUTE_FORCE - 192.168.0.20",
                secondIncident.getTitle()
        );

        assertEquals(
                1,
                firstIncident.getAlerts().size()
        );

        assertEquals(
                1,
                secondIncident.getAlerts().size()
        );

        assertSame(
                firstIncident,
                alert.getIncident()
        );

        assertSame(
                secondIncident,
                secondAlert.getIncident()
        );

        verify(repository, times(2))
                .save(any(Incident.class));
    }
    
    @Test
    void shouldFindIncidentById() {

        Incident incident = new Incident();

        incident.setId(1L);
        incident.setTitle("BRUTE_FORCE - 192.168.0.10");
        incident.setDescription("Test incident");
        incident.setSeverity(Severity.HIGH);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedAt(Instant.now());
        incident.setUpdateAt(Instant.now());

        when(repository.findById(1L))
                .thenReturn(Optional.of(incident));

        IncidentResponse response =
                incidentService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals(
                "BRUTE_FORCE - 192.168.0.10",
                response.title()
        );
        assertEquals(
                IncidentStatus.OPEN,
                response.status()
        );

        verify(repository).findById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenIncidentDoesNotExist() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.findById(999L)
        );

        verify(repository).findById(999L);
    }
    
    @Test
    void shouldFindAllIncidents() {

        Incident incident = new Incident();

        incident.setId(1L);
        incident.setTitle("BRUTE_FORCE - 192.168.0.10");
        incident.setDescription("Test incident");
        incident.setSeverity(Severity.HIGH);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedAt(Instant.now());
        incident.setUpdateAt(Instant.now());

        Page<Incident> page =
                new PageImpl<>(List.of(incident));

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

        Page<IncidentResponse> response =
        		incidentService.findAll(
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "BRUTE_FORCE - 192.168.0.10",
                response.getContent().get(0).title()
        );

        verify(repository).findAll(pageable);
    }
    
    @Test
    void shouldFilterIncidentsByStatus() {

        Incident incident = new Incident();

        incident.setId(1L);
        incident.setTitle("BRUTE_FORCE - 192.168.0.10");
        incident.setStatus(IncidentStatus.OPEN);
        incident.setSeverity(Severity.HIGH);
        incident.setCreatedAt(Instant.now());
        incident.setUpdateAt(Instant.now());

        Page<Incident> page =
                new PageImpl<>(List.of(incident));

        Pageable pageable =
                PageRequest.of(0, 20);

        when(repository.findAll(
                ArgumentMatchers.<Specification<Incident>>any(),
                eq(pageable)
        )).thenReturn(page);

        Page<IncidentResponse> response =
        		incidentService.findAll(
                        IncidentStatus.OPEN,
                        null,
                        null,
                        pageable
                );

        assertEquals(1, response.getTotalElements());

        verify(repository).findAll(
                ArgumentMatchers.<Specification<Incident>>any(),
                eq(pageable)
        );
    }
    
    @Test
    void shouldReturnNullWhenAlertIsNull() {

        Incident result = incidentService.processAlert(null);

        assertNull(result);

        verifyNoInteractions(repository);
    }
    
    @Test
    void shouldUseUnknownWhenAlertHasNoSecurityEvent() {

        Alert alertWithoutEvent = new Alert();

        alertWithoutEvent.setRule("BRUTE_FORCE");
        alertWithoutEvent.setSeverity(Severity.HIGH);
        alertWithoutEvent.setStatus(AlertStatus.OPEN);
        alertWithoutEvent.setSecurityEvent(null);

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - unknown"
        )).thenReturn(Optional.empty());

        when(repository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident result =
                incidentService.processAlert(alertWithoutEvent);

        assertNotNull(result);

        assertEquals(
                "BRUTE_FORCE - unknown",
                result.getTitle()
        );

        assertEquals(
                1,
                result.getAlerts().size()
        );

        assertSame(
                alertWithoutEvent,
                result.getAlerts().get(0)
        );

        verify(repository).save(any(Incident.class));
    }
    
    @Test
    void shouldUseUnknownWhenSourceIpIsBlank() {

        SecurityEvent eventWithoutIp = new SecurityEvent();
        eventWithoutIp.setSourceip("   ");

        Alert alertWithoutIp = new Alert();

        alertWithoutIp.setRule("BRUTE_FORCE");
        alertWithoutIp.setSeverity(Severity.HIGH);
        alertWithoutIp.setStatus(AlertStatus.OPEN);
        alertWithoutIp.setSecurityEvent(eventWithoutIp);

        when(repository.findFirstByStatusAndTitleOrderByCreatedAtDesc(
                IncidentStatus.OPEN,
                "BRUTE_FORCE - unknown"
        )).thenReturn(Optional.empty());

        when(repository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident result =
                incidentService.processAlert(alertWithoutIp);

        assertNotNull(result);

        assertEquals(
                "BRUTE_FORCE - unknown",
                result.getTitle()
        );

        verify(repository).save(any(Incident.class));
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
                () -> incidentService.findAll(
                        null,
                        from,
                        to,
                        pageable
                )
        );

        verifyNoInteractions(repository);
    }
    
    
}