package com.sentinelops.dto;

import java.time.Instant;
import java.util.List;

import com.sentinelops.model.Incident;
import com.sentinelops.model.IncidentStatus;
import com.sentinelops.model.Severity;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        Severity severity,
        IncidentStatus status,
        Instant createdAt,
        Instant updatedAt,
        int alertCount,
        List<IncidentNoteResponse> notes
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
                incident.getAlerts().size(),
                incident.getNotes()
                        .stream()
                        .map(IncidentNoteResponse::fromEntity)
                        .toList()
        );
    }
}