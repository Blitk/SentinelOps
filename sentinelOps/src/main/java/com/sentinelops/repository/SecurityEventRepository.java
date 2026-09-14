package com.sentinelops.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sentinelops.model.SecurityEvent;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long>{

	List<SecurityEvent> findTop100ByOrderByTimestampDesc();
	
	List<SecurityEvent> findByTimestampAfterOrderByTimestampDesc(Instant timestamp);
	
}
