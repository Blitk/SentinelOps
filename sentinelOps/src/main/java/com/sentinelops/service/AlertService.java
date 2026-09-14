package com.sentinelops.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.sentinelops.detection.DetectionResult;
import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.repository.AlertRepository;

@Service
public class AlertService {
	
	private final AlertRepository repository;
	
	public AlertService(AlertRepository repository) {
		
		this.repository = repository;
	}
	
	public Alert createAlert(DetectionResult result, SecurityEvent event) {
		
		Alert alert = new Alert();
		
		alert.setCreatedAt(Instant.now());
		alert.setSeverity(result.severity());
		alert.setRule(result.rule());
		alert.setDescription(result.description())
		alert.setStatus(AlertStatus.OPEN);
		alert.setSecurityEvent(event);
		
		return repository.save(alert);
		
	}
	
}
