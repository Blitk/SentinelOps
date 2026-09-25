package com.sentinelops.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sentinelops.model.Alert;
import com.sentinelops.model.AlertStatus;
import com.sentinelops.model.Severity;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {
	
	long countByStatus(AlertStatus status);

	long countBySeverity(Severity severity);
	
	long countByCreatedAtGreaterThanEqual(Instant createdAt);
	
	@Query("""
	        SELECT a.rule, COUNT(a)
	        FROM Alert a
	        GROUP BY a.rule
	        ORDER BY COUNT(a) DESC
	        """)
	List<Object[]> countAlertsByRule();
	
	@Query("""
	        SELECT a.severity, COUNT(a)
	        FROM Alert a
	        GROUP BY a.severity
	        ORDER BY COUNT(a) DESC
	        """)
	List<Object[]> countAlertsBySeverity();
	
	@Query("""
	        SELECT a.securityEvent.sourceip, COUNT(a)
	        FROM Alert a
	        WHERE a.securityEvent.sourceip IS NOT NULL
	        GROUP BY a.securityEvent.sourceip
	        ORDER BY COUNT(a) DESC
	        """)
	List<Object[]> countAlertsBySourceIp(Pageable pageable);
	
}