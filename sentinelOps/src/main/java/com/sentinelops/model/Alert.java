package com.sentinelops.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name= "alerts")
@Getter
@Setter
public class Alert {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Instant createdAt;
	
	@Enumerated(EnumType.STRING)
	private Severity severity;
	
	private String rule;
	
	private String description;
	
	@Enumerated(EnumType.STRING)
	private AlertStatus status;
	
	@ManyToOne
	@JoinColumn(name="security_event_id")
	private SecurityEvent securityEvent;
	
	@ManyToOne
	@JoinColumn(name="incident_id")
	private Incident incident;
}
