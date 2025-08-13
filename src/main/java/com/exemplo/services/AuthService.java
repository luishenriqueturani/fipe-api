package com.exemplo.services;

import com.exemplo.entities.ApiClient;
import com.exemplo.enums.AppRole;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class AuthService {
	private final JwtKeyService jwtKeyService;

	public AuthService(JwtKeyService jwtKeyService) {
		this.jwtKeyService = jwtKeyService;
	}

	@Transactional
	public String issueTokenForClient(String clientId, String clientSecret) {
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

        return Jwt
				.issuer("https://fipe-api.local")
				.subject(client.clientId)
				.expiresAt(exp)
				.issuedAt(now)
                .groups(AppRole.API_CLIENT.role())
				.claim("client_id", client.clientId)
				.claim("token_version", client.tokenVersion)
				.jws()
				.keyId(key.kid())
				.sign(key.privateKey());
	}
}
