package com.sentinelops.dto;

public record SourceIpMetricResponse(
        String sourceip,
        long count
) {}