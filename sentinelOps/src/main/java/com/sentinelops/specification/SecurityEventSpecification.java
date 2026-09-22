package com.sentinelops.specification;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;

import com.sentinelops.model.SecurityEvent;

public class SecurityEventSpecification {

    private SecurityEventSpecification() {
    }

    public static Specification<SecurityEvent> hasSourceip(
            String sourceip) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("sourceip"),
                        sourceip
                );
    }

    public static Specification<SecurityEvent> hasMethod(
            String method) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("method"),
                        method
                );
    }

    public static Specification<SecurityEvent> hasStatuscode(
            Integer statuscode) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("statuscode"),
                        statuscode
                );
    }
    
    public static Specification<SecurityEvent> timestampGreaterThanOrEqualTo(
            Instant from) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("timestamp"),
                        from
                );
    }

    public static Specification<SecurityEvent> timestampLessThanOrEqualTo(
            Instant to) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("timestamp"),
                        to
                );
    }
    
}