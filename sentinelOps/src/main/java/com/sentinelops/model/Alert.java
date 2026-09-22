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


@Entity
@Table(name= "alerts")

public class Alert {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AlertStatus getStatus() {
		return status;
	}

	public void setStatus(AlertStatus status) {
		this.status = status;
	}

	public SecurityEvent getSecurityEvent() {
		return securityEvent;
	}

	public void setSecurityEvent(SecurityEvent securityEvent) {
		this.securityEvent = securityEvent;
	}

	public Incident getIncident() {
		return incident;
	}

	public void setIncident(Incident incident) {
		this.incident = incident;
	}

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
