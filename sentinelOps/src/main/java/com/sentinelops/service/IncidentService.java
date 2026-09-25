package com.sentinelops.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import com.sentinelops.dto.IncidentResponse;
import com.sentinelops.dto.IncidentStatusRequest;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.exception.ResourceNotFoundException;
import com.sentinelops.model.Incident;
import com.sentinelops.model.IncidentStatus;
import com.sentinelops.specification.IncidentSpecification;
import com.sentinelops.model.Alert;
import com.sentinelops.repository.IncidentRepository;

@Service
public class IncidentService {

    private final IncidentRepository repository;

    public IncidentService(IncidentRepository repository) {
        this.repository = repository;
    }
    
    @Transactional
    public IncidentResponse updateStatus(
            Long id,
            IncidentStatusRequest request) {

        Incident incident = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Incident not found with id: " + id
                ));

        if (request.status() == null) {
            throw new InvalidSecurityEventException(
                    "Incident status cannot be null"
            );
        }

        incident.setStatus(request.status());
        incident.setUpdateAt(Instant.now());

        Incident savedIncident = repository.save(incident);

        return IncidentResponse.fromEntity(savedIncident);
    }

    @Transactional
    public Incident processAlert(Alert alert) {

        if (alert == null) {
            return null;
        }

        String title = buildIncidentTitle(alert);

        Incident incident = repository
                .findFirstByStatusAndTitleOrderByCreatedAtDesc(
                        IncidentStatus.OPEN,
                        title
                )
                .orElseGet(() -> createIncident(alert, title));

        incident.addAlert(alert);

        incident.setUpdateAt(Instant.now());

        return repository.save(incident);
    }

    private Incident createIncident(
            Alert alert,
            String title) {

        Incident incident = new Incident();

        incident.setTitle(title);
        incident.setDescription(
                "Security incident generated from detection rule: "
                        + alert.getRule()
        );
        incident.setSeverity(alert.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedAt(Instant.now());
        incident.setUpdateAt(Instant.now());

        return incident;
    }

    private String buildIncidentTitle(Alert alert) {

        String sourceIp = "unknown";

        if (alert.getSecurityEvent() != null
                && alert.getSecurityEvent().getSourceip() != null
                && !alert.getSecurityEvent().getSourceip().isBlank()) {

            sourceIp = alert.getSecurityEvent().getSourceip();
        }

        return alert.getRule() + " - " + sourceIp;
    }
    
    @Transactional(readOnly = true)
    public Page<IncidentResponse> findAll(
            IncidentStatus status,
            Instant from,
            Instant to,
            Pageable pageable) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidSecurityEventException(
                    "'from' cannot be after 'to'");
        }

        Specification<Incident> specification = null;

        if (status != null) {
            specification =
                    IncidentSpecification.hasStatus(status);
        }

        if (from != null) {
            Specification<Incident> fromSpecification =
                    IncidentSpecification
                            .createdAtGreaterThanOrEqualTo(from);

            specification = specification == null
                    ? fromSpecification
                    : specification.and(fromSpecification);
        }

        if (to != null) {
            Specification<Incident> toSpecification =
                    IncidentSpecification
                            .createdAtLessThanOrEqualTo(to);

            specification = specification == null
                    ? toSpecification
                    : specification.and(toSpecification);
        }

        Page<Incident> incidents =
                specification == null
                        ? repository.findAll(pageable)
                        : repository.findAll(specification, pageable);

        return incidents.map(IncidentResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public IncidentResponse findById(Long id) {

        Incident incident = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Incident not found with id: " + id));

        return IncidentResponse.fromEntity(incident);
    }
    
    
}