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
@Table(name = "session")
public class Session extends PanacheEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "api_client_id")
	public ApiClient apiClient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_user_id")
	public AdminUser adminUser;

	@Column(name = "token_jti", nullable = false, length = 255, unique = true)
	public String tokenJti; // JWT ID único do token

	@Column(name = "ip_address", length = 64)
	public String ipAddress;

	@Column(name = "user_agent", length = 255)
	public String userAgent;

	@Column(name = "login_at", nullable = false)
	public LocalDateTime loginAt;

	@Column(name = "last_activity_at", nullable = false)
	public LocalDateTime lastActivityAt;

	@Column(name = "expires_at", nullable = false)
	public LocalDateTime expiresAt;

	@Column(name = "is_active", nullable = false)
	public boolean isActive = true;

	@Column(name = "logout_at")
	public LocalDateTime logoutAt;

	@Column(name = "created_at", nullable = false)
	public LocalDateTime createdAt;

	@Column(name = "updated_at")
	public LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.loginAt == null) {
			this.loginAt = this.createdAt;
		}
		if (this.lastActivityAt == null) {
			this.lastActivityAt = this.createdAt;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt) || !this.isActive;
	}
}

