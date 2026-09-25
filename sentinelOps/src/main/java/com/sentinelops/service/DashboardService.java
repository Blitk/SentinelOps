package com.sentinelops.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelops.dto.AlertRuleMetricResponse;
import com.sentinelops.dto.AlertSeverityMetricResponse;
import com.sentinelops.dto.DashboardMetricsResponse;
import com.sentinelops.dto.DashboardResponse;
import com.sentinelops.dto.EventTimeMetricResponse;
import com.sentinelops.dto.SourceIpMetricResponse;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.IncidentStatus;
import com.sentinelops.model.Severity;
import com.sentinelops.repository.AlertRepository;
import com.sentinelops.repository.IncidentRepository;
import com.sentinelops.repository.SecurityEventRepository;

@Service
public class DashboardService {

    private final SecurityEventRepository securityEventRepository;
    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final DetectionRuleService detectionRuleService;

    public DashboardService(
            SecurityEventRepository securityEventRepository,
            AlertRepository alertRepository,
            IncidentRepository incidentRepository,
            DetectionRuleService detectionRuleService) {

        this.securityEventRepository = securityEventRepository;
        this.alertRepository = alertRepository;
        this.incidentRepository = incidentRepository;
        this.detectionRuleService = detectionRuleService;
    }
    
    @Transactional(readOnly = true)
    public List<AlertSeverityMetricResponse> getAlertsBySeverity() {

        return alertRepository.countAlertsBySeverity()
                .stream()
                .map(row -> new AlertSeverityMetricResponse(
                        row[0].toString(),
                        (Long) row[1]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        long totalEvents =
                securityEventRepository.count();

        long totalAlerts =
                alertRepository.count();

        long openAlerts =
                alertRepository.countByStatus(
                        AlertStatus.OPEN
                );

        long criticalAlerts =
                alertRepository.countBySeverity(
                        Severity.CRITICAL
                );

        long totalIncidents =
                incidentRepository.count();

        long openIncidents =
                incidentRepository.countByStatus(
                        IncidentStatus.OPEN
                );

        long activeRules =
                detectionRuleService.findAll().size();

        return new DashboardResponse(
                totalEvents,
                totalAlerts,
                openAlerts,
                criticalAlerts,
                totalIncidents,
                openIncidents,
                activeRules
        );
    }
    
    @Transactional(readOnly = true)
    public List<AlertRuleMetricResponse> getAlertsByRule() {

        return alertRepository.countAlertsByRule()
                .stream()
                .map(row -> new AlertRuleMetricResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public DashboardMetricsResponse getMetrics() {

        Instant now = Instant.now();

        Instant oneHourAgo =
                now.minus(1, ChronoUnit.HOURS);

        Instant twentyFourHoursAgo =
                now.minus(24, ChronoUnit.HOURS);

        long eventsLastHour =
                securityEventRepository
                        .countByTimestampGreaterThanEqual(oneHourAgo);

        long alertsLast24Hours =
                alertRepository
                        .countByCreatedAtGreaterThanEqual(
                                twentyFourHoursAgo
                        );

        long incidentsLast24Hours =
                incidentRepository
                        .countByCreatedAtGreaterThanEqual(
                                twentyFourHoursAgo
                        );

        return new DashboardMetricsResponse(
                eventsLastHour,
                alertsLast24Hours,
                incidentsLast24Hours
        );
    }
    
    @Transactional(readOnly = true)
    public List<SourceIpMetricResponse> getEventsBySourceIp() {

        Pageable topTen = PageRequest.of(0, 10);

        return securityEventRepository
                .countEventsBySourceIp(topTen)
                .stream()
                .map(row -> new SourceIpMetricResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<EventTimeMetricResponse> getEventsByHour() {

        Instant from =
                Instant.now().minus(24, ChronoUnit.HOURS);

        return securityEventRepository
                .countEventsByHour(from)
                .stream()
                .map(row -> new EventTimeMetricResponse(
                        ((java.sql.Timestamp) row[0]).toInstant(),
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<SourceIpMetricResponse> getAlertsBySourceIp() {

        Pageable topTen = PageRequest.of(0, 10);

        return alertRepository
                .countAlertsBySourceIp(topTen)
                .stream()
                .map(row -> new SourceIpMetricResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }
}