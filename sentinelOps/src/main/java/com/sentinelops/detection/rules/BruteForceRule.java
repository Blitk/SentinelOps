package com.sentinelops.detection.rules;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

@Component
public class BruteForceRule implements DetectionRule{
	
	private static final String RULE_NAME = "BruteForceRule";
	
	private static final int FAILURE_THRESHOLD = 5;
	
	@Override
	public String GetName() {
		
		return RULE_NAME;
		
	}
	
	@Override
	public DetectionResult evaluate(List<SecurityEvent> events) {
		
		if(events == null || events.isEmpty()) {
			
			return DetectionResult.notDetected(RULE_NAME);
			
		}
		
		long failedRequests = events.stream()
				.filter(event -> event != null)
				.filter(event -> event.getStatuscode() == 401)
				.count();
		
		if(failedRequests >= FAILURE_THRESHOLD) {
			
			return DetectionResult.detected(RULE_NAME, Severity.HIGH, "Possible brute-force attack detected: "+failedRequests+" unauthorized requests.");
		}
		
		return DetectionResult.notDetected(RULE_NAME);
		
	}
	;
}
