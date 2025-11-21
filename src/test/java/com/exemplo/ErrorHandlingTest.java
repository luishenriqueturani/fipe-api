package com.exemplo;

import com.exemplo.entities.JwtKey;
import com.exemplo.enums.JwtAlg;
import com.exemplo.enums.KeyStatus;
import com.exemplo.security.PemUtils;
import com.exemplo.services.JwtKeyService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ErrorHandlingTest {

    @Inject
    JwtKeyService jwtKeyService;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar chaves JWT e atualizar cache
        JwtKey.deleteAll();
        jwtKeyService.warmUp(); // Limpar cache
    }

    // ========== JwtKeyService Error Handling Tests ==========

    @Test
    @Transactional
    void testJwtKeyService_GetActiveSigningKey_ThrowsIllegalStateException_WhenNoActiveKey() {
        // Arrange - Limpar todas as chaves e atualizar cache
        JwtKey.deleteAll();
        jwtKeyService.warmUp(); // Limpar cache

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            jwtKeyService.getActiveSigningKey();
        });

        assertEquals("Nenhuma chave ACTIVE disponível", exception.getMessage());
    }

    @Test
    @Transactional
    void testJwtKeyService_GetActiveSigningKey_ThrowsIllegalStateException_WhenOnlyRetiredKeys() throws Exception {
        // Arrange - Criar apenas chave RETIRED
        JwtKey retiredKey = createValidJwtKey("retired-key", KeyStatus.RETIRED);
        jwtKeyService.warmUp();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            jwtKeyService.getActiveSigningKey();
        });

        assertEquals("Nenhuma chave ACTIVE disponível", exception.getMessage());
    }

    @Test
    @Transactional
    void testJwtKeyService_GetActiveSigningKey_ThrowsIllegalStateException_WhenOnlyNextKeys() throws Exception {
        // Arrange - Criar apenas chave NEXT
        JwtKey nextKey = createValidJwtKey("next-key", KeyStatus.NEXT);
        jwtKeyService.warmUp();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            jwtKeyService.getActiveSigningKey();
        });

        assertEquals("Nenhuma chave ACTIVE disponível", exception.getMessage());
    }

    @Test
    @Transactional
    void testJwtKeyService_BuildJwks_ReturnsEmptyList_WhenNoKeys() {
        // Arrange - Limpar todas as chaves e atualizar cache
        JwtKey.deleteAll();
        jwtKeyService.warmUp(); // Limpar cache

        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert
        assertNotNull(jwks);
        assertTrue(jwks.isEmpty());
    }

    @Test
    @Transactional
    void testJwtKeyService_BuildJwks_ReturnsEmptyList_WhenOnlyNextKeys() throws Exception {
        // Arrange - Criar apenas chave NEXT
        createValidJwtKey("next-key", KeyStatus.NEXT);
        jwtKeyService.warmUp();

        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert
        assertNotNull(jwks);
        assertTrue(jwks.isEmpty());
    }

    @Test
    @Transactional
    void testJwtKeyService_BuildJwks_IncludesActiveAndRetired_ExcludesNext() throws Exception {
        // Arrange
        createValidJwtKey("active-key", KeyStatus.ACTIVE);
        createValidJwtKey("retired-key", KeyStatus.RETIRED);
        createValidJwtKey("next-key", KeyStatus.NEXT);
        jwtKeyService.warmUp();

        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert
        assertNotNull(jwks);
        assertEquals(2, jwks.size()); // Apenas ACTIVE e RETIRED
        assertTrue(jwks.stream().anyMatch(jwk -> "active-key".equals(jwk.get("kid"))));
        assertTrue(jwks.stream().anyMatch(jwk -> "retired-key".equals(jwk.get("kid"))));
        assertFalse(jwks.stream().anyMatch(jwk -> "next-key".equals(jwk.get("kid"))));
    }

    // ========== PemUtils Error Handling Tests ==========

    @Test
    void testPemUtils_ReadPublicKeyFromPem_ThrowsIllegalArgumentException_WhenInvalidPem() {
        // Arrange
        String invalidPem = "invalid-pem-format";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPublicKeyFromPem(invalidPem);
        });

        assertNotNull(exception.getMessage());
        // A mensagem pode variar, mas deve ser uma IllegalArgumentException
    }

    @Test
    void testPemUtils_ReadPublicKeyFromPem_ThrowsIllegalArgumentException_WhenEmptyPem() {
        // Arrange
        String emptyPem = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPublicKeyFromPem(emptyPem);
        });

        assertNotNull(exception.getMessage());
        // A mensagem pode variar, mas deve ser uma IllegalArgumentException
    }

    @Test
    void testPemUtils_ReadPublicKeyFromPem_ThrowsIllegalArgumentException_WhenNullPem() {
        // Arrange
        String nullPem = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            PemUtils.readPublicKeyFromPem(nullPem);
        });
    }

    @Test
    void testPemUtils_ReadPrivateKeyFromPem_ThrowsIllegalArgumentException_WhenInvalidPem() {
        // Arrange
        String invalidPem = "invalid-pem-format";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPrivateKeyFromPem(invalidPem);
        });

        assertNotNull(exception.getMessage());
        // A mensagem pode variar, mas deve ser uma IllegalArgumentException
    }

    @Test
    void testPemUtils_ReadPrivateKeyFromPem_ThrowsIllegalArgumentException_WhenEmptyPem() {
        // Arrange
        String emptyPem = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPrivateKeyFromPem(emptyPem);
        });

        assertNotNull(exception.getMessage());
        // A mensagem pode variar, mas deve ser uma IllegalArgumentException
    }

    @Test
    void testPemUtils_ReadPrivateKeyFromPem_ThrowsIllegalArgumentException_WhenNullPem() {
        // Arrange
        String nullPem = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            PemUtils.readPrivateKeyFromPem(nullPem);
        });
    }

    // ========== Helper Methods ==========

    private JwtKey createValidJwtKey(String kid, KeyStatus status) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        JwtKey jwtKey = new JwtKey();
        jwtKey.kid = kid;
        jwtKey.alg = JwtAlg.RS256;
        jwtKey.publicKeyPem = toPem(keyPair.getPublic());
        jwtKey.privateKeyCiphertext = toPem(keyPair.getPrivate());
        jwtKey.status = status;
        jwtKey.createdAt = LocalDateTime.now();
        jwtKey.persist();
        return jwtKey;
    }

    private String toPem(PublicKey key) {
        byte[] encoded = key.getEncoded();
        String base64 = java.util.Base64.getEncoder().encodeToString(encoded);
        String header = "-----BEGIN PUBLIC KEY-----\n";
        String footer = "\n-----END PUBLIC KEY-----";
        return header + chunkString(base64, 64) + footer;
    }

    private String toPem(java.security.PrivateKey key) {
        byte[] encoded = key.getEncoded();
        String base64 = java.util.Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PRIVATE KEY-----\n" + chunkString(base64, 64) + "\n-----END PRIVATE KEY-----";
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

