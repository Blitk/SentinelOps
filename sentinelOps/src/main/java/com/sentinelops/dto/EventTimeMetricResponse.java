package com.sentinelops.dto;

import java.time.Instant;

public record EventTimeMetricResponse(
        Instant hour,
        long count
) {}