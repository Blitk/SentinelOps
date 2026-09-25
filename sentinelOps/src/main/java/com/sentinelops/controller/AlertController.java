package com.sentinelops.controller;


import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sentinelops.dto.AlertResponse;
import com.sentinelops.dto.AlertStatusRequest;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.Severity;
import com.sentinelops.service.AlertService;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AlertStatusRequest request) {

        return ResponseEntity.ok(
                service.updateStatus(id, request)
        );
    }
    
    @GetMapping
    public ResponseEntity<Page<AlertResponse>> findAll(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String rule,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<AlertResponse> response =
                service.findAll(
                        status,
                        severity,
                        rule,
                        from,
                        to,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }
}