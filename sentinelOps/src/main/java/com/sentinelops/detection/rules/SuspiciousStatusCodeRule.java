package com.sentinelops.detection.rules;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.detection.DetectionRule;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.model.Severity;

@Component
public class SuspiciousStatusCodeRule implements DetectionRule{

	private static final String RULE_NAME = "SuspiciousStatusCodeRule";
	
	@Override
	public String getName() {
		
		return RULE_NAME;
		
	}
	
	@Override
	public DetectionResult evaluate(List<SecurityEvent> events) {
		
		if(events == null || events.isEmpty()) {
			
			return DetectionResult.notDetected(RULE_NAME);
			
		}
		
		return events.stream()
			.filter(event -> event != null)
			.filter(event -> event.getStatuscode() != null)
			.filter(event -> event.getStatuscode() == 401)
			.findFirst()
			.map(event -> DetectionResult.detected(RULE_NAME, Severity.MEDIUM, "Unathorized request detected.", event.getSourceip()))
			.orElseGet(() -> DetectionResult.notDetected(RULE_NAME));
		
	}
	
}
