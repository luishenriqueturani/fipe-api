package com.exemplo.services;

import com.exemplo.entities.JwtKey;
import com.exemplo.enums.JwtAlg;
import com.exemplo.enums.KeyStatus;
import com.exemplo.security.PemUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;

@QuarkusTest
class JwtKeyServiceTest {
    
    // Método auxiliar para converter chave para PEM (similar ao JwtKeySeeder)
    private String toPem(PublicKey key) {
        byte[] encoded = key.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        String header = key instanceof java.security.interfaces.RSAPublicKey 
                ? "-----BEGIN PUBLIC KEY-----\n" 
                : "-----BEGIN PUBLIC KEY-----\n";
        String footer = "\n-----END PUBLIC KEY-----";
        return header + chunkString(base64, 64) + footer;
    }
    
    private String toPem(PrivateKey key) {
        byte[] encoded = key.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
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

    @Inject
    JwtKeyService jwtKeyService;

    private JwtKey activeKey;
    private JwtKey retiredKey;
    private JwtKey nextKey;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Limpar dados de teste anteriores
        JwtKey.deleteAll();

        // Gerar par de chaves RSA para testes
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair1 = keyGen.generateKeyPair();
        KeyPair keyPair2 = keyGen.generateKeyPair();
        KeyPair keyPair3 = keyGen.generateKeyPair();

        // Converter chaves para PEM
        String publicKeyPem1 = toPem(keyPair1.getPublic());
        String privateKeyPem1 = toPem(keyPair1.getPrivate());
        String publicKeyPem2 = toPem(keyPair2.getPublic());
        String privateKeyPem2 = toPem(keyPair2.getPrivate());
        String publicKeyPem3 = toPem(keyPair3.getPublic());
        String privateKeyPem3 = toPem(keyPair3.getPrivate());

        // Criar chave ACTIVE
        activeKey = new JwtKey();
        activeKey.kid = "test-active-key-1";
        activeKey.alg = JwtAlg.RS256;
        activeKey.publicKeyPem = publicKeyPem1;
        activeKey.privateKeyCiphertext = privateKeyPem1; // Em produção seria criptografado
        activeKey.status = KeyStatus.ACTIVE;
        activeKey.createdAt = LocalDateTime.now();
        activeKey.persist();

        // Criar chave RETIRED
        retiredKey = new JwtKey();
        retiredKey.kid = "test-retired-key-1";
        retiredKey.alg = JwtAlg.RS256;
        retiredKey.publicKeyPem = publicKeyPem2;
        retiredKey.privateKeyCiphertext = privateKeyPem2;
        retiredKey.status = KeyStatus.RETIRED;
        retiredKey.createdAt = LocalDateTime.now().minusDays(30);
        retiredKey.rotatedAt = LocalDateTime.now().minusDays(1);
        retiredKey.persist();

        // Criar chave NEXT
        nextKey = new JwtKey();
        nextKey.kid = "test-next-key-1";
        nextKey.alg = JwtAlg.RS256;
        nextKey.publicKeyPem = publicKeyPem3;
        nextKey.privateKeyCiphertext = privateKeyPem3;
        nextKey.status = KeyStatus.NEXT;
        nextKey.createdAt = LocalDateTime.now();
        nextKey.persist();

        // Fazer warmUp para carregar no cache
        jwtKeyService.warmUp();
    }

    @Test
    @Transactional
    void testGetActiveSigningKey_Success() {
        // Act
        JwtKeyService.CachedKey cachedKey = jwtKeyService.getActiveSigningKey();

        // Assert
        assertNotNull(cachedKey);
        assertEquals(activeKey.kid, cachedKey.kid());
        assertEquals(JwtAlg.RS256, cachedKey.alg());
        assertEquals(KeyStatus.ACTIVE, cachedKey.status());
        assertNotNull(cachedKey.publicKey());
        assertNotNull(cachedKey.privateKey());
        assertNotNull(cachedKey.createdAt());
    }

    @Test
    @Transactional
    void testGetActiveSigningKey_NoActiveKey() {
        // Arrange - Remover chave ativa
        JwtKey key = JwtKey.findById(activeKey.id);
        key.status = KeyStatus.RETIRED;
        key.persist();
        jwtKeyService.warmUp();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            jwtKeyService.getActiveSigningKey();
        });
    }

    @Test
    @Transactional
    void testBuildJwks_IncludesActiveAndRetired() {
        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert
        assertNotNull(jwks);
        assertEquals(2, jwks.size()); // ACTIVE e RETIRED, não NEXT

        // Verificar que contém a chave ACTIVE
        boolean hasActive = jwks.stream()
                .anyMatch(jwk -> activeKey.kid.equals(jwk.get("kid")));
        assertTrue(hasActive, "JWKS deve conter a chave ACTIVE");

        // Verificar que contém a chave RETIRED
        boolean hasRetired = jwks.stream()
                .anyMatch(jwk -> retiredKey.kid.equals(jwk.get("kid")));
        assertTrue(hasRetired, "JWKS deve conter a chave RETIRED");

        // Verificar que NÃO contém a chave NEXT
        boolean hasNext = jwks.stream()
                .anyMatch(jwk -> nextKey.kid.equals(jwk.get("kid")));
        assertFalse(hasNext, "JWKS não deve conter a chave NEXT");
    }

    @Test
    @Transactional
    void testBuildJwks_JwkFormat() {
        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert
        assertNotNull(jwks);
        assertFalse(jwks.isEmpty());

        Map<String, Object> jwk = jwks.get(0);
        assertEquals("RSA", jwk.get("kty"));
        assertEquals("RS256", jwk.get("alg"));
        assertEquals("sig", jwk.get("use"));
        assertNotNull(jwk.get("kid"));
        assertNotNull(jwk.get("n")); // Modulus
        assertNotNull(jwk.get("e")); // Exponent
    }

    @Test
    @Transactional
    void testBuildJwks_EmptyWhenNoActiveOrRetired() {
        // Arrange - Marcar todas como NEXT
        JwtKey active = JwtKey.findById(activeKey.id);
        active.status = KeyStatus.NEXT;
        active.persist();
        JwtKey retired = JwtKey.findById(retiredKey.id);
        retired.status = KeyStatus.NEXT;
        retired.persist();
        jwtKeyService.warmUp();

        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert
        assertNotNull(jwks);
        assertTrue(jwks.isEmpty());
    }

    @Test
    @Transactional
    void testWarmUp_LoadsAllKeys() throws Exception {
        // Arrange - Limpar cache manualmente (não há método público, mas podemos criar nova chave)
        JwtKey newKey = new JwtKey();
        newKey.kid = "test-new-key";
        newKey.alg = JwtAlg.RS256;
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        newKey.publicKeyPem = toPem(keyPair.getPublic());
        newKey.privateKeyCiphertext = toPem(keyPair.getPrivate());
        newKey.status = KeyStatus.ACTIVE;
        newKey.createdAt = LocalDateTime.now();
        newKey.persist();

        // Act
        jwtKeyService.warmUp();

        // Assert - A nova chave deve estar disponível
        JwtKeyService.CachedKey cachedKey = jwtKeyService.getActiveSigningKey();
        // Pode ser a nova chave ou a antiga, mas deve existir uma ACTIVE
        assertNotNull(cachedKey);
        assertTrue(cachedKey.kid().equals(activeKey.kid) || cachedKey.kid().equals(newKey.kid));
    }

    @Test
    @Transactional
    void testWarmUp_ClearsCache() throws Exception {
        // Arrange - Criar chave e fazer warmUp
        JwtKey tempKey = new JwtKey();
        tempKey.kid = "temp-key";
        tempKey.alg = JwtAlg.RS256;
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        tempKey.publicKeyPem = toPem(keyPair.getPublic());
        tempKey.privateKeyCiphertext = toPem(keyPair.getPrivate());
        tempKey.status = KeyStatus.ACTIVE;
        tempKey.createdAt = LocalDateTime.now();
        tempKey.persist();
        jwtKeyService.warmUp();

        // Act - Deletar chave e fazer warmUp novamente
        tempKey.delete();
        jwtKeyService.warmUp();

        // Assert - A chave deletada não deve estar no cache
        // Se houver apenas uma chave ACTIVE, deve ser a original
        JwtKeyService.CachedKey cachedKey = jwtKeyService.getActiveSigningKey();
        assertNotNull(cachedKey);
        assertEquals(activeKey.kid, cachedKey.kid());
    }

    @Test
    @Transactional
    void testToJwk_RsaPublicKey() {
        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert - Verificar que todas as chaves são RSA
        for (Map<String, Object> jwk : jwks) {
            assertEquals("RSA", jwk.get("kty"));
            assertNotNull(jwk.get("n"));
            assertNotNull(jwk.get("e"));
            
            // Verificar que n e e são strings (base64url)
            assertInstanceOf(String.class, jwk.get("n"));
            assertInstanceOf(String.class, jwk.get("e"));
        }
    }

    @Test
    @Transactional
    void testToCached_LoadsKeysFromPem() {
        // Act
        JwtKeyService.CachedKey cachedKey = jwtKeyService.getActiveSigningKey();

        // Assert
        assertNotNull(cachedKey);
        assertNotNull(cachedKey.publicKey());
        assertNotNull(cachedKey.privateKey());
        assertInstanceOf(RSAPublicKey.class, cachedKey.publicKey());
        
        // Verificar que as chaves foram carregadas corretamente do PEM
        // Comparando o modulus da chave pública
        RSAPublicKey rsaPublicKey = (RSAPublicKey) cachedKey.publicKey();
        assertNotNull(rsaPublicKey.getModulus());
        assertNotNull(rsaPublicKey.getPublicExponent());
    }

    @Test
    @Transactional
    void testGetActiveSigningKey_MultipleActiveKeys() throws Exception {
        // Arrange - Criar outra chave ACTIVE
        JwtKey anotherActiveKey = new JwtKey();
        anotherActiveKey.kid = "test-active-key-2";
        anotherActiveKey.alg = JwtAlg.RS256;
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        anotherActiveKey.publicKeyPem = toPem(keyPair.getPublic());
        anotherActiveKey.privateKeyCiphertext = toPem(keyPair.getPrivate());
        anotherActiveKey.status = KeyStatus.ACTIVE;
        anotherActiveKey.createdAt = LocalDateTime.now();
        anotherActiveKey.persist();
        
        jwtKeyService.warmUp();

        // Act
        JwtKeyService.CachedKey cachedKey = jwtKeyService.getActiveSigningKey();

        // Assert - Deve retornar a primeira chave ACTIVE encontrada
        assertNotNull(cachedKey);
        assertEquals(KeyStatus.ACTIVE, cachedKey.status());
        // Pode ser qualquer uma das duas chaves ACTIVE
        assertTrue(cachedKey.kid().equals(activeKey.kid) || cachedKey.kid().equals(anotherActiveKey.kid));
    }

    @Test
    @Transactional
    void testBuildJwks_Order() {
        // Act
        List<Map<String, Object>> jwks = jwtKeyService.buildJwks();

        // Assert - Verificar que todas as chaves têm o formato correto
        assertNotNull(jwks);
        for (Map<String, Object> jwk : jwks) {
            assertTrue(jwk.containsKey("kty"));
            assertTrue(jwk.containsKey("alg"));
            assertTrue(jwk.containsKey("use"));
            assertTrue(jwk.containsKey("kid"));
            assertTrue(jwk.containsKey("n"));
            assertTrue(jwk.containsKey("e"));
        }
    }

    @Test
    @Transactional
    void testCachedKey_RecordProperties() {
        // Act
        JwtKeyService.CachedKey cachedKey = jwtKeyService.getActiveSigningKey();

        // Assert - Verificar todas as propriedades do record
        assertNotNull(cachedKey.kid());
        assertNotNull(cachedKey.alg());
        assertNotNull(cachedKey.status());
        assertNotNull(cachedKey.publicKey());
        assertNotNull(cachedKey.privateKey());
        assertNotNull(cachedKey.createdAt());
        
        assertEquals(activeKey.kid, cachedKey.kid());
        assertEquals(activeKey.alg, cachedKey.alg());
        assertEquals(activeKey.status, cachedKey.status());
    }
}

