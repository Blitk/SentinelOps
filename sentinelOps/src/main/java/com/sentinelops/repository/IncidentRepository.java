package com.sentinelops.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sentinelops.model.Incident;
import com.sentinelops.model.IncidentStatus;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    Optional<Incident> findFirstByStatusAndTitleOrderByCreatedAtDesc(
            IncidentStatus status,
            String title
    );
    
    long countByStatus(IncidentStatus status);

    long countByCreatedAtGreaterThanEqual(Instant createdAt);
    
}