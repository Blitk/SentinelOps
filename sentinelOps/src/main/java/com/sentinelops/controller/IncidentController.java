package com.sentinelops.controller;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sentinelops.dto.IncidentResponse;
import com.sentinelops.model.IncidentStatus;
import com.sentinelops.service.IncidentService;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> findAll(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<IncidentResponse> response =
                service.findAll(
                        status,
                        from,
                        to,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }
}