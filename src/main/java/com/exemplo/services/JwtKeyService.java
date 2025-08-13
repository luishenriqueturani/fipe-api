package com.exemplo.services;

import com.exemplo.entities.JwtKey;
import com.exemplo.security.PemUtils;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
@Startup
public class JwtKeyService {
	private final Map<String, CachedKey> cacheByKid = new ConcurrentHashMap<>();

	@Transactional
	public void warmUp() {
		List<JwtKey> keys = JwtKey.listAll();
		for (JwtKey k : keys) {
			cacheByKid.put(k.kid, toCached(k));
		}
	}

	public CachedKey getActiveSigningKey() {
        return cacheByKid.values().stream()
                .filter(k -> k.status == com.exemplo.enums.KeyStatus.ACTIVE)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Nenhuma chave ACTIVE disponível"));
	}

	public List<Map<String, Object>> buildJwks() {
        return cacheByKid.values().stream()
                .filter(k -> k.status == com.exemplo.enums.KeyStatus.ACTIVE || k.status == com.exemplo.enums.KeyStatus.RETIRED)
				.map(this::toJwk)
				.collect(Collectors.toList());
	}

	private Map<String, Object> toJwk(CachedKey k) {
		PublicKey publicKey = k.publicKey;
		if (publicKey instanceof RSAPublicKey rsa) {
            return Map.of(
					"kty", "RSA",
                    "alg", k.alg.name(),
					"use", "sig",
					"kid", k.kid,
					"n", PemUtils.jwkN(rsa),
					"e", PemUtils.jwkE(rsa)
			);
		}
		throw new IllegalStateException("Apenas RSA suportado neste exemplo");
	}

	private CachedKey toCached(JwtKey key) {
		PublicKey publicKey = PemUtils.readPublicKeyFromPem(key.publicKeyPem);
		PrivateKey privateKey = PemUtils.readPrivateKeyFromPem(key.privateKeyCiphertext);
		return new CachedKey(key.kid, key.alg, key.status, publicKey, privateKey, key.createdAt);
	}

    public record CachedKey(
            String kid,
            com.exemplo.enums.JwtAlg alg,
            com.exemplo.enums.KeyStatus status,
            PublicKey publicKey,
            PrivateKey privateKey,
            LocalDateTime createdAt
    ) {}
}
