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
	
	private static final long COOLDOWN_SECONDS = 300;

	private final AlertRepository repository;

	private final AlertCooldownService cooldownService;
	
	public AlertService(AlertRepository repository, AlertCooldownService cooldownService) {
		
		this.cooldownService = cooldownService;

		this.repository = repository;
	}
	
	public Alert createAlert(DetectionResult result, SecurityEvent event) {
		
		String cooldownKey = buildCooldownKey(result);

		boolean cooldownStarted = cooldownService.startCooldown(cooldownKey, COOLDOWN_SECONDS);

		if(!cooldownStarted){

			return null;
		}

		Alert alert = new Alert();
		
		alert.setCreatedAt(Instant.now());
		alert.setSeverity(result.severity());
		alert.setRule(result.rule());
		alert.setDescription(result.description())
		alert.setStatus(AlertStatus.OPEN);
		alert.setSecurityEvent(event);

		return repository.save(alert);
		
	}

	private String buildCooldownKey(DetectionResult result){

		String sourceip = result.sourceip();

		if(sourceip == null || sourceip.isBlank()){

			sourceip = "global";

		}

		return "sentinelops:alert:"+result.rule()+":"+sourceip;

	}
	
}
