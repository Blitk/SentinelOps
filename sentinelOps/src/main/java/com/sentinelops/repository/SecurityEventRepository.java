package com.sentinelops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sentinelops.model.SecurityEvent;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long>{

}
