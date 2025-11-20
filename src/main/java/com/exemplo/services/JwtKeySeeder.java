package com.exemplo.services;

import com.exemplo.entities.JwtKey;
import com.exemplo.enums.JwtAlg;
import com.exemplo.enums.KeyStatus;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class JwtKeySeeder {

	private static final Logger LOG = Logger.getLogger(JwtKeySeeder.class);

	@Transactional
	void onStart(@Observes @Priority(1) StartupEvent ev) {
		LOG.info("Verificando se chave JWT ativa existe...");

		// Verificar se já existe uma chave ACTIVE
		JwtKey existingKey = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
		
		if (existingKey != null) {
			LOG.info("Chave JWT ativa já existe: " + existingKey.kid);
			return;
		}

		// Gerar nova chave RSA
		LOG.info("Nenhuma chave JWT ativa encontrada. Gerando nova chave...");
		
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048); // Tamanho da chave RSA
			KeyPair keyPair = keyGen.generateKeyPair();
			
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

			// Converter chaves para formato PEM
			String publicKeyPem = toPemPublicKey(publicKey);
			String privateKeyPem = toPemPrivateKey(privateKey);

			// Criar chave JWT
			JwtKey jwtKey = new JwtKey();
			jwtKey.kid = UUID.randomUUID().toString().replace("-", "").substring(0, 32); // KID de 32 caracteres
			jwtKey.alg = JwtAlg.RS256;
			jwtKey.publicKeyPem = publicKeyPem;
			jwtKey.privateKeyCiphertext = privateKeyPem; // Em produção, criptografar esta chave
			jwtKey.status = KeyStatus.ACTIVE;
			jwtKey.createdAt = LocalDateTime.now();
			jwtKey.persist();

			LOG.info("Chave JWT criada com sucesso! KID: " + jwtKey.kid);
			LOG.warn("ATENÇÃO: A chave privada está armazenada sem criptografia. Em produção, use criptografia de envelope!");
			
		} catch (Exception e) {
			LOG.error("Erro ao gerar chave JWT: " + e.getMessage(), e);
			throw new RuntimeException("Falha ao criar chave JWT inicial", e);
		}
	}

	private String toPemPublicKey(RSAPublicKey publicKey) {
		try {
			byte[] encoded = publicKey.getEncoded();
			String base64 = Base64.getEncoder().encodeToString(encoded);
			return "-----BEGIN PUBLIC KEY-----\n" +
					chunkString(base64, 64) +
					"\n-----END PUBLIC KEY-----";
		} catch (Exception e) {
			throw new RuntimeException("Erro ao converter chave pública para PEM", e);
		}
	}

	private String toPemPrivateKey(RSAPrivateKey privateKey) {
		try {
			byte[] encoded = privateKey.getEncoded();
			String base64 = Base64.getEncoder().encodeToString(encoded);
			return "-----BEGIN PRIVATE KEY-----\n" +
					chunkString(base64, 64) +
					"\n-----END PRIVATE KEY-----";
		} catch (Exception e) {
			throw new RuntimeException("Erro ao converter chave privada para PEM", e);
		}
	}

	private String chunkString(String str, int chunkSize) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i += chunkSize) {
			if (i > 0) {
				sb.append("\n");
			}
			int end = Math.min(i + chunkSize, str.length());
			sb.append(str.substring(i, end));
		}
		return sb.toString();
	}
}

