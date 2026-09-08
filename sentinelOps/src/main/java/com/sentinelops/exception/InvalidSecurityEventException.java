package com.sentinelops.exception;

public class InvalidSecurityEventException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public InvalidSecurityEventException(String message) {
		
		super(message);
		
	}
	
}
