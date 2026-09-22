package com.sentinelops.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.dto.SecurityEventResponse;
import com.sentinelops.exception.InvalidSecurityEventException;
import com.sentinelops.exception.ResourceNotFoundException;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.service.SecurityEventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/events")
public class SecurityEventController {

    private final SecurityEventService service;

    public SecurityEventController(SecurityEventService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SecurityEventResponse> receiveEvent(
            @Valid @RequestBody SecurityEventRequest request) {

        SecurityEvent event = service.receiveEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SecurityEventResponse.fromEntity(event));
    }

    @GetMapping
    public ResponseEntity<Page<SecurityEventResponse>> findAll(

            @RequestParam(required = false)
            String sourceip,

            @RequestParam(required = false)
            String method,

            @RequestParam(required = false)
            Integer statuscode,

            @RequestParam(required = false)
            Instant from,

            @RequestParam(required = false)
            Instant to,

            @PageableDefault(
                    size = 20,
                    sort = "timestamp",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidSecurityEventException(
                    "'from' cannot be after 'to'"
            );
        }

        Page<SecurityEventResponse> response =
                service.findAll(
                        sourceip,
                        method,
                        statuscode,
                        from,
                        to,
                        pageable
                )
                .map(SecurityEventResponse::fromEntity);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecurityEventResponse> findById(
            @PathVariable Long id) {

        SecurityEvent event = service.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Security event not found with id: " + id
                        )
                );

        return ResponseEntity.ok(
                SecurityEventResponse.fromEntity(event)
        );
    }
}