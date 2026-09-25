package com.sentinelops.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinelops.detection.DetectionRule;
import com.sentinelops.dto.DetectionRuleResponse;

@Service
public class DetectionRuleService {

    private final List<DetectionRule> rules;

    public DetectionRuleService(List<DetectionRule> rules) {
        this.rules = rules;
    }

    public List<DetectionRuleResponse> findAll() {

        return rules.stream()
                .map(rule -> new DetectionRuleResponse(
                        rule.getName(),
                        rule.getDescription()
                ))
                .toList();
    }
}