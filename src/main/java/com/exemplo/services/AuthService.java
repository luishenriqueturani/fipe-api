package com.exemplo.services;

import com.exemplo.entities.AdminUser;
import com.exemplo.entities.ApiClient;
import com.exemplo.entities.Session;
import com.exemplo.enums.AppRole;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@ApplicationScoped
public class AuthService {
	private final JwtKeyService jwtKeyService;
	
	@Inject
	SessionService sessionService;

	public AuthService(JwtKeyService jwtKeyService) {
		this.jwtKeyService = jwtKeyService;
	}

	@Transactional
	public String issueTokenForClient(String clientId, String clientSecret, String ipAddress, String userAgent) {
		ApiClient client = ApiClient.find("clientId = ?1 and isActive = true", clientId).firstResult();
		if (client == null) {
			return null;
		}
		// Em produção, compare hash
		if (!client.clientSecret.equals(clientSecret)) {
			return null;
		}

		var key = jwtKeyService.getActiveSigningKey();
		Instant now = Instant.now();
		Instant exp = now.plus(Duration.ofMinutes(10));
		LocalDateTime expiresAt = LocalDateTime.ofInstant(exp, java.time.ZoneId.systemDefault());

		// Criar sessão
		Session session = sessionService.createSessionForClient(client, ipAddress, userAgent, expiresAt);

        String jwt = Jwt
				.issuer("https://fipe-api.local")
				.subject(client.clientId)
				.expiresAt(exp)
				.issuedAt(now)
                .groups(AppRole.API_CLIENT.role())
				.claim("client_id", client.clientId)
				.claim("token_version", client.tokenVersion)
				.jti(session.tokenJti)
				.jws()
				.keyId(key.kid())
				.sign(key.privateKey());

		return jwt;
	}

	@Transactional
	public String issueTokenForAdmin(String username, String password, String ipAddress, String userAgent) {
		AdminUser admin = AdminUser.find("username = ?1 and isActive = true and deletedAt is null", username).firstResult();
		if (admin == null) {
			return null;
		}
		// Em produção, compare hash
		if (!admin.password.equals(password)) {
			return null;
		}

		// Atualizar último login
		admin.lastLoginAt = LocalDateTime.now();
		admin.persist();

		var key = jwtKeyService.getActiveSigningKey();
		Instant now = Instant.now();
		Instant exp = now.plus(Duration.ofHours(8)); // Admin tem token com validade maior
		LocalDateTime expiresAt = LocalDateTime.ofInstant(exp, java.time.ZoneId.systemDefault());

		// Criar sessão
		Session session = sessionService.createSessionForAdmin(admin, ipAddress, userAgent, expiresAt);

        String jwt = Jwt
				.issuer("https://fipe-api.local")
				.subject(admin.username)
				.expiresAt(exp)
				.issuedAt(now)
                .groups(AppRole.ADMIN.role())
				.claim("admin_id", admin.id)
				.claim("username", admin.username)
				.jti(session.tokenJti)
				.jws()
				.keyId(key.kid())
				.sign(key.privateKey());

		return jwt;
	}
}
