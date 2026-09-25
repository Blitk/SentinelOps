package com.sentinelops.dto;

public record DashboardMetricsResponse(
        long eventsLastHour,
        long alertsLast24Hours,
        long incidentsLast24Hours
) {
	
	
}