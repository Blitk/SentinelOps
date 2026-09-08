package com.sentinelops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.service.SecurityEventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/events")
public class SecurityEventController {
	
	@Autowired
	private SecurityEventService service;
	
	@PostMapping
	public ResponseEntity<SecurityEvent> receiveEvent(
			@Valid @RequestBody SecurityEventRequest request){
		
		SecurityEvent event = service.receiveEvent(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(event);
		
	}
}
