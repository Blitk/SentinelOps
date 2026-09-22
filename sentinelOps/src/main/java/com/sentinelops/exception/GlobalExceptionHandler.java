package com.sentinelops.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


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
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
	        ResourceNotFoundException exception) {

	    ErrorResponse response = new ErrorResponse(
	            Instant.now(),
	            HttpStatus.NOT_FOUND.value(),
	            "Resource not found",
	            exception.getMessage()
	    );

	    return ResponseEntity
	            .status(HttpStatus.NOT_FOUND)
	            .body(response);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception exception){
		
		ErrorResponse response = new ErrorResponse(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal server error",
				"An unexpected error occurred"
		);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(response);
	}
	
}
