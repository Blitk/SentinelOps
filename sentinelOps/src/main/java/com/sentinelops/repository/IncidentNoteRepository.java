package com.sentinelops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sentinelops.model.IncidentNote;

@Repository
public interface IncidentNoteRepository
        extends JpaRepository<IncidentNote, Long> {

    List<IncidentNote> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}