package com.sentinelops.detection;

import java.util.List;

import com.sentinelops.model.SecurityEvent;

public interface DetectionRule {

	String getName();
	DetectionResult evaluate(List<SecurityEvent> events);
	
}
