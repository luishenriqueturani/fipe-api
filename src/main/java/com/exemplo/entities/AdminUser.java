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
@Table(name = "admin_user", uniqueConstraints = {
		@UniqueConstraint(name = "uk_admin_user_username", columnNames = {"username"})
})
public class AdminUser extends PanacheEntity {
	@Column(name = "name", nullable = false, length = 150)
	public String name;

	@Column(name = "username", nullable = false, length = 64)
	public String username;

	@Column(name = "password", nullable = false, length = 200)
	public String password; // Em produção, armazene hash

	@Column(name = "email", nullable = false, length = 255)
	public String email;

	@Column(name = "is_active", nullable = false)
	public boolean isActive = true;

	@Column(name = "last_login_at")
	public LocalDateTime lastLoginAt;

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

