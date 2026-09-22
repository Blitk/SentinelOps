package com.sentinelops.dto;

import java.time.Instant;

import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.Severity;

public record AlertResponse(
        Long id,
        Instant createdAt,
        Severity severity,
        String rule,
        String description,
        AlertStatus status,
        Long securityEventId,
        Long incidentId
) {

    public static AlertResponse fromEntity(Alert alert) {

        Long securityEventId = null;
        Long incidentId = null;

        if (alert.getSecurityEvent() != null) {
            securityEventId = alert.getSecurityEvent().getId();
        }

        if (alert.getIncident() != null) {
            incidentId = alert.getIncident().getId();
        }

        return new AlertResponse(
                alert.getId(),
                alert.getCreatedAt(),
                alert.getSeverity(),
                alert.getRule(),
                alert.getDescription(),
                alert.getStatus(),
                securityEventId,
                incidentId
        );
    }
}