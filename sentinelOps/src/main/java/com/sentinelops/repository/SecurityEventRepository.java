package com.sentinelops.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sentinelops.model.SecurityEvent;


@Repository
public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long>,
                JpaSpecificationExecutor<SecurityEvent> {

    List<SecurityEvent> findTop100ByOrderByTimestampDesc();

    List<SecurityEvent> findByTimestampAfterOrderByTimestampDesc(
            Instant timestamp
    );

    long count();

    long countByTimestampGreaterThanEqual(Instant timestamp);

    @Query("""
            SELECT e.sourceip, COUNT(e)
            FROM SecurityEvent e
            WHERE e.sourceip IS NOT NULL
            GROUP BY e.sourceip
            ORDER BY COUNT(e) DESC
            """)
    List<Object[]> countEventsBySourceIp(Pageable pageable);

    @Query(value = """
            SELECT date_trunc('hour', "timestamp") AS hour,
                   COUNT(*) AS count
            FROM security_events
            WHERE "timestamp" >= :from
            GROUP BY date_trunc('hour', "timestamp")
            ORDER BY hour
            """, nativeQuery = true)
    List<Object[]> countEventsByHour(
            @Param("from") Instant from
    );
}
