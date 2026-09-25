package com.sentinelops.dto;

import com.sentinelops.model.AlertStatus;

public record AlertStatusRequest(
        AlertStatus status
) {}