package com.exemplo.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_access_log")
public class ApiAccessLog extends PanacheEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "api_client_id")
	public ApiClient apiClient;

	@Column(name = "requested_at", nullable = false)
	public LocalDateTime requestedAt;

	@Column(name = "method", nullable = false, length = 10)
	public String method;

	@Column(name = "path", nullable = false, length = 512)
	public String path;

	@Column(name = "query", length = 1024)
	public String query;

	@Column(name = "status_code")
	public Integer statusCode;

	@Column(name = "ip", length = 64)
	public String ip;

	@Column(name = "user_agent", length = 255)
	public String userAgent;

	@Column(name = "duration_ms")
	public Long durationMs;

	@Column(name = "created_at", nullable = false)
	public LocalDateTime createdAt;

	@Column(name = "updated_at")
	public LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	public LocalDateTime deletedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.requestedAt == null) {
			this.requestedAt = this.createdAt;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
