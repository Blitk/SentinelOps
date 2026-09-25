package com.sentinelops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidentNoteRequest(

        @NotBlank(message = "Author is required")
        @Size(max = 100, message = "Author must have at most 100 characters")
        String author,

        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Content must have at most 2000 characters")
        String content
) {}