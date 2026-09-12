package com.sentinelops.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.repository.SecurityEventRepository;



@Service
public class SecurityEventService {
	
	@Autowired
	private SecurityEventRepository repository;
	
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
		return repository.save(event);
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