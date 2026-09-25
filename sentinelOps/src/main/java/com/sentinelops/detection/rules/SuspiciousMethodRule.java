package com.sentinelops.detection.rules;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.DetectionRule;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

@Component
public class SuspiciousMethodRule implements DetectionRule {

    private static final String RULE_NAME = "SuspiciousMethodRule";

    @Override
    public String getName() {
        return RULE_NAME;
    }
    
    @Override
    public String getDescription() {
        return "Detects suspicious HTTP methods such as TRACE and CONNECT.";
    }

    @Override
    public DetectionResult evaluate(List<SecurityEvent> events) {

        if (events == null || events.isEmpty()) {
            return DetectionResult.notDetected(RULE_NAME);
        }

        return events.stream()
                .filter(event -> event != null)
                .filter(event -> event.getMethod() != null)
                .filter(event ->
                        event.getMethod().equalsIgnoreCase("TRACE")
                        || event.getMethod().equalsIgnoreCase("CONNECT"))
                .findFirst()
                .map(event -> DetectionResult.detected(
                        RULE_NAME,
                        Severity.MEDIUM,
                        "Suspicious HTTP method detected: "
                                + event.getMethod(),
                        event.getSourceip(),
                        event.getId()
                ))
                .orElseGet(() ->
                        DetectionResult.notDetected(RULE_NAME)
                );
    }
}