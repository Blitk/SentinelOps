package com.sentinelops.dto;

import java.time.Instant;

import com.sentinelops.model.SecurityEvent;

public record SecurityEventResponse(
        Long id,
        Instant timestamp,
        String sourceip,
        String method,
        String path,
        Integer statuscode,
        String source,
        Instant receivedAt
) {

    public static SecurityEventResponse fromEntity(
            SecurityEvent event) {

        return new SecurityEventResponse(
                event.getId(),
                event.getTimestamp(),
                event.getSourceip(),
                event.getMethod(),
                event.getPath(),
                event.getStatuscode(),
                event.getSource(),
                event.getReceivedAt()
        );
    }
}