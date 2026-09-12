package com.sentinelops.detection;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinelops.model.SecurityEvent;

@Service
public class DetectionEngine {
	
	private final List<DetectionRule> rules;
	
	public DetectionEngine(List<DetectionRule> rules) {
		
		this.rules = rules;
		
	}
	
	public List<DetectionResult> analyze(List<SecurityEvent> events){
		
		return rules.stream()
				.map(rule -> rule.evaluate(events))
				.filter(DetectionResult::detected)
				.toList();
		
	}
	
}
