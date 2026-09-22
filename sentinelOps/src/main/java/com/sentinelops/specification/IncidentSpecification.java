package com.sentinelops.specification;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;

import com.sentinelops.model.Incident;
import com.sentinelops.model.IncidentStatus;

public class IncidentSpecification {

    private IncidentSpecification() {
    }

    public static Specification<Incident> hasStatus(
            IncidentStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Incident> createdAtGreaterThanOrEqualTo(
            Instant from) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );
    }

    public static Specification<Incident> createdAtLessThanOrEqualTo(
            Instant to) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        to
                );
    }
}