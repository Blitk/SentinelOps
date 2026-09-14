package com.sentinelops.detection.rules;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.DetectionRule;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

@Component
public class BruteForceRule implements DetectionRule{
	
	private static final String RULE_NAME = "BruteForceRule";
	
	private static final int FAILURE_THRESHOLD = 5;
	
	@Override
	public String getName() {
		
		return RULE_NAME;
		
	}
	
	@Override
	public DetectionResult evaluate(List<SecurityEvent> events) {
		
		if(events == null || events.isEmpty()) {
			
			return DetectionResult.notDetected(RULE_NAME);
			
		}
		
		Map<String, Long> failedAttemptsByIp = events.stream()
				.filter(event -> event != null)
				.filter(event -> event.getStatuscode() != null)
				.filter(event -> event.getStatuscode() == 401)
				.filter(event -> event.getSourceip() != null)
				.collect(Collectors.groupingBy(SecurityEvent::getSourceip, Collectors.counting()));
				
		return failedAttemptsByIp.entrySet()
				.stream()
				.filter(entry -> entry.getValue() >= FAILURE_THRESHOLD)
				.findFirst()
				.map(entry -> DetectionResult.detected(
						RULE_NAME,
						Severity.HIGH,
						"Possible Brute-Force attack detected from IP "
							+entry.getKey()
							+" with "
							+entry.getValue()
							+" failed authentication attempts.",
						entry.getKey()

				))
				.orElseGet(() -> DetectionResult.notDetected(RULE_NAME));
		
	}
	;
}
