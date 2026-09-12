package com.sentinelops.detection.rules;

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
	public DetectionResult evaluate(SecurityEvent event) {
		
		if(event == null || event.getStatuscode() == null) {
			
			return DetectionResult.notDetected(RULE_NAME);
			
		}
		
		if(event.getStatusCode() == 401) {
			
			return DetectionResult.detected(RULE_NAME, Severity.MEDIUM, "Unauthorized request detected.");
			
		}
		
		return DetectionResult.notDetected(RULE_NAME);
		
	}
	
}
