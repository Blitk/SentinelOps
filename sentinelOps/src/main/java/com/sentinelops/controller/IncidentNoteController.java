package com.sentinelops.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.sentinelops.dto.IncidentNoteRequest;
import com.sentinelops.dto.IncidentNoteResponse;
import com.sentinelops.service.IncidentNoteService;

@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/notes")
public class IncidentNoteController {

    private final IncidentNoteService service;

    public IncidentNoteController(
            IncidentNoteService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<IncidentNoteResponse> createNote(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentNoteRequest request) {

        return ResponseEntity.ok(
                service.createNote(incidentId, request)
        );
    }

    @GetMapping
    public ResponseEntity<List<IncidentNoteResponse>> findByIncidentId(
            @PathVariable Long incidentId) {

        return ResponseEntity.ok(
                service.findByIncidentId(incidentId)
        );
    }
    
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long incidentId,
            @PathVariable Long noteId) {

        service.deleteNote(incidentId, noteId);

        return ResponseEntity.noContent().build();
    }
    
}