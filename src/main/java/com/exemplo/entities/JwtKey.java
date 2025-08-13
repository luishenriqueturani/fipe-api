package com.exemplo.entities;

import com.exemplo.enums.JwtAlg;
import com.exemplo.enums.KeyStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_key", uniqueConstraints = {
		@UniqueConstraint(name = "uk_jwt_key_kid", columnNames = {"kid"})
})
public class JwtKey extends PanacheEntity {
	@Column(name = "kid", nullable = false, length = 64)
	public String kid;

	@Enumerated(EnumType.STRING)
	@Column(name = "alg", nullable = false, length = 16)
	public JwtAlg alg; // RS256, ES256, etc

	@Column(name = "public_key_pem", nullable = false, columnDefinition = "TEXT")
	public String publicKeyPem;

	@Column(name = "private_key_ciphertext", nullable = false, columnDefinition = "TEXT")
	public String privateKeyCiphertext; // chave privada criptografada (envelope)

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	public KeyStatus status; // ACTIVE, NEXT, RETIRED

	@Column(name = "created_at", nullable = false)
	public LocalDateTime createdAt;

	@Column(name = "updated_at")
	public LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	public LocalDateTime deletedAt;

	@Column(name = "rotated_at")
	public LocalDateTime rotatedAt;

	@Column(name = "expires_at")
	public LocalDateTime expiresAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
