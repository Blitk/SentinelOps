package com.sentinelops.model;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="security_events")
@Getter
@Setter
public class SecurityEvent {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	
	private Instant timestamp;
	
	private String sourceip;
	
	private String method;
	
	private String path;
	
	private Integer statuscode;
	
	private String source;
	
	private Instant receivedAt;
	
	
	
	
}
