package com.sentinelops.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.dto.AlertResponse;
import com.sentinelops.dto.AlertStatusRequest;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.exception.ResourceNotFoundException;
import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;
import com.sentinelops.repository.AlertRepository;
import com.sentinelops.specification.AlertSpecification;

@Service
public class AlertService {

    private static final long COOLDOWN_SECONDS = 300;

    private final AlertRepository repository;
    
    private final AlertCooldownService cooldownService;
    
    private final IncidentService incidentService;

    
    public AlertService(
            AlertRepository repository, AlertCooldownService cooldownService, IncidentService incidentService) {

        this.repository = repository;
        
        this.cooldownService = cooldownService;
        
        this.incidentService = incidentService;
    }

    public Alert createAlert(
            DetectionResult result,
            SecurityEvent event) {

        String cooldownKey = buildCooldownKey(result);

        boolean cooldownStarted =
                cooldownService.startCoolDown(
                        cooldownKey,
                        COOLDOWN_SECONDS
                );

        if (!cooldownStarted) {
            return null;
        }

        Alert alert = new Alert();

        alert.setCreatedAt(Instant.now());
        alert.setSeverity(result.severity());
        alert.setRule(result.rule());
        alert.setDescription(result.description());
        alert.setStatus(AlertStatus.OPEN);
        alert.setSecurityEvent(event);

        Alert savedAlert = repository.save(alert);

        incidentService.processAlert(savedAlert);

        return savedAlert;
    }

    private String buildCooldownKey(DetectionResult result) {

        String sourceip = result.sourceip();

        if (sourceip == null || sourceip.isBlank()) {
            sourceip = "global";
        }

        return "sentinelops:alert:"
                + result.rule()
                + ":"
                + sourceip;
    }
    
    @Transactional(readOnly = true)
    public Page<AlertResponse> findAll(
            AlertStatus status,
            Severity severity,
            String rule,
            Instant from,
            Instant to,
            Pageable pageable) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidSecurityEventException(
                    "'from' cannot be after 'to'");
        }

        Specification<Alert> specification = null;

        if (status != null) {
            specification = AlertSpecification.hasStatus(status);
        }

        if (severity != null) {
            Specification<Alert> severitySpecification =
                    AlertSpecification.hasSeverity(severity);

            specification = specification == null
                    ? severitySpecification
                    : specification.and(severitySpecification);
        }

        if (rule != null && !rule.isBlank()) {
            Specification<Alert> ruleSpecification =
                    AlertSpecification.hasRule(rule.trim());

            specification = specification == null
                    ? ruleSpecification
                    : specification.and(ruleSpecification);
        }

        if (from != null) {
            Specification<Alert> fromSpecification =
                    AlertSpecification.createdAtGreaterThanOrEqualTo(from);

            specification = specification == null
                    ? fromSpecification
                    : specification.and(fromSpecification);
        }

        if (to != null) {
            Specification<Alert> toSpecification =
                    AlertSpecification.createdAtLessThanOrEqualTo(to);

            specification = specification == null
                    ? toSpecification
                    : specification.and(toSpecification);
        }

        Page<Alert> alerts =
                specification == null
                        ? repository.findAll(pageable)
                        : repository.findAll(specification, pageable);

        return alerts.map(AlertResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public AlertResponse findById(Long id) {

        Alert alert = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with id: " + id));

        return AlertResponse.fromEntity(alert);
    }
    
    
    
    @Transactional
    public AlertResponse updateStatus(
            Long id,
            AlertStatusRequest request) {

        Alert alert = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with id: " + id
                ));

        if (request.status() == null) {
            throw new InvalidSecurityEventException(
                    "Alert status cannot be null"
            );
        }

        alert.setStatus(request.status());

        Alert savedAlert = repository.save(alert);

        return AlertResponse.fromEntity(savedAlert);
    }
    
}