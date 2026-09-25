package com.sentinelops.dto;

import java.time.Instant;

import com.sentinelops.model.IncidentNote;

public record IncidentNoteResponse(
        Long id,
        String author,
        String content,
        Instant createdAt
) {

    public static IncidentNoteResponse fromEntity(
            IncidentNote note) {

        return new IncidentNoteResponse(
                note.getId(),
                note.getAuthor(),
                note.getContent(),
                note.getCreatedAt()
        );
    }
}