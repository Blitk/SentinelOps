package com.sentinelops.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelops.detection.DetectionEngine;
import com.sentinelops.detection.DetectionResult;
import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.repository.SecurityEventRepository;



@Service
public class SecurityEventService {
	
	
	private final SecurityEventRepository repository;
	
	private final DetectionEngine detectionEngine;
	
	private final AlertService alertService;
	
	public SecurityEventService(SecurityEventRepository repository, DetectionEngine detectionEngine, AlertService alertService) {
		
		this.repository = repository;
		this.detectionEngine = detectionEngine;
		this.alertService = alertService;
		
	}
	
	private void validate(SecurityEventRequest request) {
		if(request==null) {
			throw new InvalidSecurityEventException("Security Event cannot be null");
		}
		if(request.timestamp()==null) {
			throw new InvalidSecurityEventException("Timestamp required");
		}
		if(request.sourceip()==null || request.sourceip().isBlank()) {
			throw new InvalidSecurityEventException("source ip required");
		}
		if(request.method()==null || request.method().isBlank()) {
			throw new InvalidSecurityEventException("method required");
		}
		if(request.path()==null || request.path().isBlank()) {
			throw new InvalidSecurityEventException("path required");
		}
		if(request.statuscode() < 100 || request.statuscode() > 599) {
			throw new InvalidSecurityEventException("Invalid status code");
		}
		if(request.statuscode() == null) {
			throw new InvalidSecurityEventException("Status code is required");
		}
		if(request.source()==null || request.source().isBlank()) {
			throw new InvalidSecurityEventException("Event source is required");
		}
	}
	
	@Transactional
	public SecurityEvent normalize(SecurityEventRequest request) {
		
		SecurityEvent event = new SecurityEvent();
		
		event.setSourceip(request.sourceip().trim());

        event.setMethod(request.method().trim().toUpperCase());

        event.setPath(request.path().trim());

        event.setStatuscode(request.statuscode());
        
		event.setSource(request.source().trim().toUpperCase());

        event.setReceivedAt(Instant.now());
		
		return event;
		
	}
	
	@Transactional
	public SecurityEvent receiveEvent(SecurityEventRequest request) {
		
		validate(request);
		
		SecurityEvent event = normalize(request);
		
		SecurityEvent savedEvent = repository.save(event);
		
		List<SecurityEvent> recentEvents = repository.findTop100ByOrderByTimestampDesc();
		
		List<DetectionResult> results = detectionEngine.analyze(recentEvents);
		
		for(DetectionResult result : results) {
			
			alertService.createAlert(result, savedEvent);
			
		}
		
		return savedEvent;
	}
	
	@Transactional(readOnly = true)
	public Optional<SecurityEvent> findById(Long id){
		return repository.findById(id);
	}
	
	@Transactional(readOnly = true)
	public List<SecurityEvent> findAll(){
		return repository.findAll();
	}
	
	@Transactional
	public void deleteById(Long id) {
		repository.deleteById(id);
	}
	
	
}