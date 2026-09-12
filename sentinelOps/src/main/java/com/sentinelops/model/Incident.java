package com.sentinelops.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="incidents")
@Getter
@Setter
public class Incident {
	
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
	private Instant updateAt;
	
	@OneToMany
	@JoinColumn(name= "incident_id")
	private List<Alert> alerts = new ArrayList<>();
	
}
