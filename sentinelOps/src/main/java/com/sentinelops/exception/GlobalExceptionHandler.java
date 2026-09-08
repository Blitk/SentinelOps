package com.sentinelops.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sentinelops.exception.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(InvalidSecurityEventException.class)
	public ResponseEntity<ErrorResponse> handleInvalidSecurityEvent(InvalidSecurityEventException exception){
		
		ErrorResponse response = new ErrorResponse(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				"Invalid security event",
				exception.getMessage()
		);
		
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(response);
		
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception exception){
		
		ErrorResponse response = new ErrorResponse(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal server error",
				"An unexpected error occured"
		);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(response);
	}
	
}
