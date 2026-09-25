package com.sentinelops.dto;

import com.sentinelops.model.IncidentStatus;

public record IncidentStatusRequest(
        IncidentStatus status
) {}