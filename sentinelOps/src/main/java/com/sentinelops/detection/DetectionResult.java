package com.sentinelops.detection;

import com.sentinelops.model.Severity;

public record DetectionResult(
        boolean detected,
        String rule,
        Severity severity,
        String description,
        String sourceip,
        Long securityEventId) {

    public static DetectionResult detected(
            String rule,
            Severity severity,
            String description,
            String sourceip,
            Long securityEventId) {

        return new DetectionResult(
                true,
                rule,
                severity,
                description,
                sourceip,
                securityEventId
        );
    }

    public static DetectionResult notDetected(String rule) {

        return new DetectionResult(
                false,
                rule,
                null,
                null,
                null,
                null
        );
    }
}