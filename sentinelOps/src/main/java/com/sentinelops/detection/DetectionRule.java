package com.sentinelops.detection;

import java.util.List;

import com.sentinelops.model.SecurityEvent;

public interface DetectionRule {

    String getName();

    default String getDescription() {
        return "Security detection rule";
    }

    DetectionResult evaluate(List<SecurityEvent> events);
}