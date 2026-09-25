package com.sentinelops.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinelops.dto.IncidentNoteRequest;
import com.sentinelops.dto.IncidentNoteResponse;
import com.sentinelops.model.Incident;
import com.sentinelops.model.IncidentNote;
import com.sentinelops.repository.IncidentNoteRepository;
import com.sentinelops.repository.IncidentRepository;
import com.sentinelops.exception.ResourceNotFoundException;

@Service
public class IncidentNoteService {

    private final IncidentNoteRepository noteRepository;
    private final IncidentRepository incidentRepository;

    public IncidentNoteService(
            IncidentNoteRepository noteRepository,
            IncidentRepository incidentRepository) {

        this.noteRepository = noteRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public IncidentNoteResponse createNote(
            Long incidentId,
            IncidentNoteRequest request) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Incident not found with id: " + incidentId
                ));

        IncidentNote note = new IncidentNote();

        note.setAuthor(request.author());
        note.setContent(request.content());
        note.setCreatedAt(Instant.now());

        incident.addNote(note);

        IncidentNote savedNote =
                noteRepository.save(note);

        return IncidentNoteResponse.fromEntity(savedNote);
    }
    
    @Transactional
    public void deleteNote(
            Long incidentId,
            Long noteId) {

        IncidentNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Incident note not found with id: " + noteId
                ));

        if (note.getIncident() == null
                || !note.getIncident().getId().equals(incidentId)) {

            throw new ResourceNotFoundException(
                    "Incident note not found for incident: " + incidentId
            );
        }

        noteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<IncidentNoteResponse> findByIncidentId(
            Long incidentId) {

        if (!incidentRepository.existsById(incidentId)) {
            throw new ResourceNotFoundException(
                    "Incident not found with id: " + incidentId
            );
        }

        return noteRepository
                .findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(IncidentNoteResponse::fromEntity)
                .toList();
    }
}