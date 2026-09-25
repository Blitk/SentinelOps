package com.sentinelops.detection.rules;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.DetectionRule;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

@Component
public class PathTraversalRule implements DetectionRule {

    private static final String RULE_NAME = "PathTraversalRule";

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public DetectionResult evaluate(List<SecurityEvent> events) {

        if (events == null || events.isEmpty()) {
            return DetectionResult.notDetected(RULE_NAME);
        }

        return events.stream()
                .filter(event -> event != null)
                .filter(event -> event.getPath() != null)
                .map(event -> new EventWithPath(
                        event,
                        event.getPath().toLowerCase(Locale.ROOT)
                ))
                .filter(this::isPathTraversal)
                .findFirst()
                .map(item -> DetectionResult.detected(
                        RULE_NAME,
                        Severity.HIGH,
                        "Possible Path Traversal attack detected on path "
                                + item.path(),
                        item.event().getSourceip(),
                        item.event().getId()
                ))
                .orElseGet(() ->
                        DetectionResult.notDetected(RULE_NAME)
                );
    }
    
    @Override
    public String getDescription() {
        return "Detects possible path traversal attempts in HTTP request paths.";
    }

    private boolean isPathTraversal(EventWithPath item) {

        String path = item.path();

        return path.contains("../")
                || path.contains("..\\")
                || path.contains("..%2f")
                || path.contains("..%2F")
                || path.contains("%2e%2e%2f")
                || path.contains("%2e%2e/")
                || path.contains("%2e%2e%5c")
                || path.contains("%2e%2e\\");
    }

    private record EventWithPath(
            SecurityEvent event,
            String path
    ) {
    }
}