package com.sentinelops.dto;

public record DashboardResponse(
        long totalEvents,
        long totalAlerts,
        long openAlerts,
        long criticalAlerts,
        long totalIncidents,
        long openIncidents,
        long activeRules
) {
}