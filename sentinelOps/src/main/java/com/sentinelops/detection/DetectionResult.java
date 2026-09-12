package com.sentinelops.detection;

import com.sentinelops.model.Severity;

public record DetectionResult(boolean detected,	String rule, Severity severity, String description) {
	
	public static DetectionResult detected(String rule,Severity severity,String description) {
		
		return new DetectionResult(true, rule, severity,	description);
		
	}
	
	public static DetectionResult notDetected(String rule) {
		
		return new DetectionResult(false, rule, null, null);
		
	}
	
}
