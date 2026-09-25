package com.sentinelops.detection.rules;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.DetectionRule;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

@Component
public class HighErrorRateRule implements DetectionRule {

    private static final String RULE_NAME = "HighErrorRateRule";

    private static final int ERROR_THRESHOLD = 5;

    private static final long TIME_WINDOW_MINUTES = 5;

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public DetectionResult evaluate(List<SecurityEvent> events) {

        if (events == null || events.isEmpty()) {
            return DetectionResult.notDetected(RULE_NAME);
        }

        Map<String, List<SecurityEvent>> errorsByIp =
                events.stream()
                        .filter(event -> event != null)
                        .filter(event -> event.getStatuscode() != null)
                        .filter(event -> isServerError(
                                event.getStatuscode()))
                        .filter(event -> event.getSourceip() != null)
                        .filter(event -> event.getTimestamp() != null)
                        .collect(Collectors.groupingBy(
                                SecurityEvent::getSourceip
                        ));

        for (Map.Entry<String, List<SecurityEvent>> entry
                : errorsByIp.entrySet()) {

            List<SecurityEvent> errors = entry.getValue();

            errors.sort((event1, event2) ->
                    event1.getTimestamp()
                            .compareTo(event2.getTimestamp()));

            for (int i = 0; i < errors.size(); i++) {

                Instant windowStart =
                        errors.get(i).getTimestamp();

                Instant windowEnd =
                        windowStart.plus(
                                TIME_WINDOW_MINUTES,
                                ChronoUnit.MINUTES
                        );

                List<SecurityEvent> errorsInWindow =
                        errors.stream()
                                .filter(event ->
                                        !event.getTimestamp()
                                                .isBefore(windowStart))
                                .filter(event ->
                                        !event.getTimestamp()
                                                .isAfter(windowEnd))
                                .toList();

                if (errorsInWindow.size() >= ERROR_THRESHOLD) {

                    SecurityEvent triggeringEvent =
                            errorsInWindow.get(
                                    errorsInWindow.size() - 1
                            );

                    return DetectionResult.detected(
                            RULE_NAME,
                            Severity.MEDIUM,
                            "High server error rate detected from IP "
                                    + entry.getKey()
                                    + " with "
                                    + errorsInWindow.size()
                                    + " server errors.",
                            entry.getKey(),
                            triggeringEvent.getId()
                    );
                }
            }
        }

        return DetectionResult.notDetected(RULE_NAME);
    }
    
    @Override
    public String getDescription() {
        return "Detects a high number of server errors from the same IP within a short time window.";
    }

    private boolean isServerError(Integer statusCode) {

        return statusCode == 500
                || statusCode == 502
                || statusCode == 503;
    }
}