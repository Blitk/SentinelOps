package com.sentinelops.dto;

import java.time.Instant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SecurityEventRequest(
		
		@NotNull
		Instant timestamp,
		
		@NotBlank
		String sourceip,
		
		@NotBlank
		String method,
		
		@NotBlank
		String path,
		
		@NotNull
		@Min(100)
		@Max(599)
		Integer statuscode,
		
		@NotBlank
		String source,
		
		@NotNull
		Instant receivedAt
		
		) {

}
