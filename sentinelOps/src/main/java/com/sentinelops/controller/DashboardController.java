package com.sentinelops.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinelops.dto.AlertRuleMetricResponse;
import com.sentinelops.dto.AlertSeverityMetricResponse;
import com.sentinelops.dto.DashboardMetricsResponse;
import com.sentinelops.dto.DashboardResponse;
import com.sentinelops.dto.EventTimeMetricResponse;
import com.sentinelops.dto.SourceIpMetricResponse;
import com.sentinelops.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }
    
    @GetMapping("/alerts-by-source-ip")
    public ResponseEntity<List<SourceIpMetricResponse>> getAlertsBySourceIp() {

        return ResponseEntity.ok(
                service.getAlertsBySourceIp()
        );
    }
    
    @GetMapping("/events-by-hour")
    public ResponseEntity<List<EventTimeMetricResponse>> getEventsByHour() {

        return ResponseEntity.ok(
                service.getEventsByHour()
        );
    }
    
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                service.getDashboard()
        );
    }
    
    @GetMapping("/events-by-source-ip")
    public ResponseEntity<List<SourceIpMetricResponse>> getEventsBySourceIp() {

        return ResponseEntity.ok(
                service.getEventsBySourceIp()
        );
    }
    
    @GetMapping("/alerts-by-severity")
    public ResponseEntity<List<AlertSeverityMetricResponse>> getAlertsBySeverity() {

        return ResponseEntity.ok(
                service.getAlertsBySeverity()
        );
    }
    
    @GetMapping("/alerts-by-rule")
    public ResponseEntity<List<AlertRuleMetricResponse>> getAlertsByRule() {

        return ResponseEntity.ok(
                service.getAlertsByRule()
        );
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsResponse> getMetrics() {

        return ResponseEntity.ok(
                service.getMetrics()
        );
    }
}