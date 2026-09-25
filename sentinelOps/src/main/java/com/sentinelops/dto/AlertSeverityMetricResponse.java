package com.sentinelops.dto;

public record AlertSeverityMetricResponse(
        String severity,
        long count
) {}