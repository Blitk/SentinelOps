package com.sentinelops.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinelops.dto.DetectionRuleResponse;
import com.sentinelops.service.DetectionRuleService;

@RestController
@RequestMapping("/api/v1/rules")
public class DetectionRuleController {

    private final DetectionRuleService service;

    public DetectionRuleController(
            DetectionRuleService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetectionRuleResponse>> findAll() {

        return ResponseEntity.ok(
                service.findAll()
        );
    }
}