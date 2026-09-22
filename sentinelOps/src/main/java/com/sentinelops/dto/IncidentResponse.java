package com.sentinelops.dto;

import java.time.Instant;

import com.sentinelops.model.Incident;
import com.sentinelops.model.Severity;
import com.sentinelops.model.IncidentStatus;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        Severity severity,
        IncidentStatus status,
        Instant createdAt,
        Instant updatedAt,
        int alertCount
) {

    public static IncidentResponse fromEntity(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getUpdatedAt(),
                incident.getAlerts().size()
        );
    }
}