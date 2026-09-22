package com.sentinelops.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="incidents")

public class Incident {
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public IncidentStatus getStatus() {
		return status;
	}

	public void setStatus(IncidentStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdateAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<Alert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Enumerated(EnumType.STRING)
	private Severity severity;
	
	@Enumerated(EnumType.STRING)
	private IncidentStatus status;
	
	private Instant createdAt;
	private Instant updatedAt;
	
	@OneToMany(mappedBy = "incident")
	private List<Alert> alerts = new ArrayList<>();
	
	public void addAlert(Alert alert) {

	    if (!alerts.contains(alert)) {
	        alerts.add(alert);
	    }

	    alert.setIncident(this);
	}

	public void removeAlert(Alert alert) {

	    alerts.remove(alert);

	    if (alert.getIncident() == this) {
	        alert.setIncident(null);
	    }
	}
	
}
