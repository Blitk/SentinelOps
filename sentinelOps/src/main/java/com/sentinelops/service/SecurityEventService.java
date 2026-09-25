package com.sentinelops.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelops.detection.DetectionEngine;
import com.sentinelops.detection.DetectionResult;
import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.repository.SecurityEventRepository;
import com.sentinelops.specification.SecurityEventSpecification;



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

	    if (request == null) {
	        throw new InvalidSecurityEventException(
	                "Security Event cannot be null"
	        );
	    }

	    if (request.timestamp() == null) {
	        throw new InvalidSecurityEventException(
	                "Timestamp required"
	        );
	    }

	    if (request.sourceip() == null || request.sourceip().isBlank()) {
	        throw new InvalidSecurityEventException(
	                "source ip required"
	        );
	    }

	    if (request.method() == null || request.method().isBlank()) {
	        throw new InvalidSecurityEventException(
	                "method required"
	        );
	    }

	    if (request.path() == null || request.path().isBlank()) {
	        throw new InvalidSecurityEventException(
	                "path required"
	        );
	    }

	    if (request.statuscode() == null) {
	        throw new InvalidSecurityEventException(
	                "Status code is required"
	        );
	    }

	    if (request.statuscode() < 100 || request.statuscode() > 599) {
	        throw new InvalidSecurityEventException(
	                "Invalid status code"
	        );
	    }

	    if (request.source() == null || request.source().isBlank()) {
	        throw new InvalidSecurityEventException(
	                "Event source is required"
	        );
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
        
        event.setTimestamp(request.timestamp());
		
		return event;
		
	}
	
	@Transactional
	public SecurityEvent receiveEvent(SecurityEventRequest request) {

	    validate(request);

	    SecurityEvent event = normalize(request);

	    SecurityEvent savedEvent = repository.save(event);

	    List<SecurityEvent> recentEvents =
	            repository.findTop100ByOrderByTimestampDesc();

	    List<DetectionResult> results =
	            detectionEngine.analyze(recentEvents);

	    for (DetectionResult result : results) {

	        if (result.securityEventId() == null) {
	            continue;
	        }

	        SecurityEvent detectedEvent =
	                repository.findById(result.securityEventId())
	                        .orElseThrow(() ->
	                                new IllegalStateException(
	                                        "Security event not found for detection: "
	                                                + result.securityEventId()
	                                )
	                        );

	        alertService.createAlert(
	                result,
	                detectedEvent
	        );
	    }

	    return savedEvent;
	}
	
	@Transactional(readOnly = true)
	public Optional<SecurityEvent> findById(Long id){
		return repository.findById(id);
	}
	
	
	@Transactional
	public void deleteById(Long id) {
		repository.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Page<SecurityEvent> findAll(
	        String sourceip,
	        String method,
	        Integer statuscode,
	        Instant from,
	        Instant to,
	        Pageable pageable) {

	    Specification<SecurityEvent> specification = null;

	    if (sourceip != null && !sourceip.isBlank()) {
	        specification =
	                SecurityEventSpecification.hasSourceip(
	                        sourceip.trim()
	                );
	    }

	    if (method != null && !method.isBlank()) {

	        Specification<SecurityEvent> methodSpecification =
	                SecurityEventSpecification.hasMethod(
	                        method.trim().toUpperCase()
	                );

	        specification = specification == null
	                ? methodSpecification
	                : specification.and(methodSpecification);
	    }

	    if (statuscode != null) {

	        Specification<SecurityEvent> statusSpecification =
	                SecurityEventSpecification.hasStatuscode(
	                        statuscode
	                );

	        specification = specification == null
	                ? statusSpecification
	                : specification.and(statusSpecification);
	    }
	    
	    if (from != null) {

	        Specification<SecurityEvent> fromSpecification =
	                SecurityEventSpecification
	                        .timestampGreaterThanOrEqualTo(from);

	        specification = specification == null
	                ? fromSpecification
	                : specification.and(fromSpecification);
	    }

	    if (to != null) {

	        Specification<SecurityEvent> toSpecification =
	                SecurityEventSpecification
	                        .timestampLessThanOrEqualTo(to);

	        specification = specification == null
	                ? toSpecification
	                : specification.and(toSpecification);
	    }

	    if (specification == null) {
	        return repository.findAll(pageable);
	    }

	    return repository.findAll(
	            specification,
	            pageable
	    );
	}
	
}