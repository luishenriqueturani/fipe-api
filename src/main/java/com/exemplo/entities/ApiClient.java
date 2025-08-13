package com.exemplo.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_client", uniqueConstraints = {
		@UniqueConstraint(name = "uk_api_client_client_id", columnNames = {"client_id"})
})
public class ApiClient extends PanacheEntity {
	@Column(name = "name", nullable = false, length = 150)
	public String name;

	@Column(name = "client_id", nullable = false, length = 64)
	public String clientId;

	@Column(name = "client_secret", nullable = false, length = 200)
	public String clientSecret; // Em produção, armazene hash

	@Column(name = "is_active", nullable = false)
	public boolean isActive = true;

	@Column(name = "allowed_ips", length = 1000)
	public String allowedIps;

	@Column(name = "allowed_origins", length = 1000)
	public String allowedOrigins;

	@Column(name = "rate_limit_per_minute")
	public Integer rateLimitPerMinute;

	@Column(name = "token_version", nullable = false)
	public int tokenVersion = 1;

	@Column(name = "last_access_at")
	public LocalDateTime lastAccessAt;

	@Column(name = "created_at", nullable = false)
	public LocalDateTime createdAt;

	@Column(name = "updated_at")
	public LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	public LocalDateTime deletedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
