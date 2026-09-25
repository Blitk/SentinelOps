package com.sentinelops.dto;

public record AlertRuleMetricResponse(
        String rule,
        long count
) {}