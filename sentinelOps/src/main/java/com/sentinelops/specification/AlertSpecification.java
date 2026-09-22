package com.sentinelops.specification;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;

import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.Severity;

public class AlertSpecification {

    private AlertSpecification() {
    }

    public static Specification<Alert> hasStatus(AlertStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Alert> hasSeverity(Severity severity) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("severity"),
                        severity
                );
    }

    public static Specification<Alert> hasRule(String rule) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("rule"),
                        rule
                );
    }
    
    public static Specification<Alert> createdAtGreaterThanOrEqualTo(
            Instant from) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );
    }

    public static Specification<Alert> createdAtLessThanOrEqualTo(
            Instant to) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        to
                );
    }
    
}