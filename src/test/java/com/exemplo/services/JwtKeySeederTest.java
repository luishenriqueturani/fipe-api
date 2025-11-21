package com.exemplo.services;

import com.exemplo.entities.JwtKey;
import com.exemplo.enums.JwtAlg;
import com.exemplo.enums.KeyStatus;
import com.exemplo.security.PemUtils;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class JwtKeySeederTest {

    @Inject
    JwtKeySeeder jwtKeySeeder;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste
        JwtKey.deleteAll();
    }

    @Test
    @Transactional
    void testOnStart_CreatesJwtKey_WhenNoActiveKeyExists() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);

        // Assert
        JwtKey key = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        assertNotNull(key);
        assertEquals(KeyStatus.ACTIVE, key.status);
        assertEquals(JwtAlg.RS256, key.alg);
        assertNotNull(key.kid);
        assertEquals(32, key.kid.length()); // KID deve ter 32 caracteres
        assertNotNull(key.publicKeyPem);
        assertNotNull(key.privateKeyCiphertext);
        assertNotNull(key.createdAt);
    }

    @Test
    @Transactional
    void testOnStart_DoesNotCreate_WhenActiveKeyExists() {
        // Arrange
        // Criar chave ativa existente
        JwtKey existingKey = new JwtKey();
        existingKey.kid = "existing-key-id-1234567890123456";
        existingKey.alg = JwtAlg.RS256;
        existingKey.publicKeyPem = "-----BEGIN PUBLIC KEY-----\ntest\n-----END PUBLIC KEY-----";
        existingKey.privateKeyCiphertext = "-----BEGIN PRIVATE KEY-----\ntest\n-----END PRIVATE KEY-----";
        existingKey.status = KeyStatus.ACTIVE;
        existingKey.persist();

        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);

        // Assert
        long count = JwtKey.count("status = ?1", KeyStatus.ACTIVE);
        assertEquals(1, count);
        
        JwtKey key = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        assertEquals("existing-key-id-1234567890123456", key.kid); // KID original mantido
    }

    @Test
    @Transactional
    void testOnStart_CreatesOnlyOneKey_WhenCalledMultipleTimes() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);
        jwtKeySeeder.onStart(ev); // Chamar novamente

        // Assert
        long count = JwtKey.count("status = ?1", KeyStatus.ACTIVE);
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void testOnStart_GeneratesValidRsaKeyPair() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);

        // Assert
        JwtKey key = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        assertNotNull(key);
        
        // Verificar que a chave pública PEM é válida
        assertTrue(key.publicKeyPem.startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(key.publicKeyPem.endsWith("-----END PUBLIC KEY-----"));
        
        // Verificar que a chave privada PEM é válida
        assertTrue(key.privateKeyCiphertext.startsWith("-----BEGIN PRIVATE KEY-----"));
        assertTrue(key.privateKeyCiphertext.endsWith("-----END PRIVATE KEY-----"));
        
        // Verificar que é possível ler a chave pública
        try {
            PublicKey publicKey = PemUtils.readPublicKeyFromPem(key.publicKeyPem);
            assertNotNull(publicKey);
            assertEquals("RSA", publicKey.getAlgorithm());
        } catch (Exception e) {
            fail("Erro ao ler chave pública PEM: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    void testOnStart_SetsCorrectDefaultValues() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);

        // Assert
        JwtKey key = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        assertNotNull(key);
        assertEquals(JwtAlg.RS256, key.alg);
        assertEquals(KeyStatus.ACTIVE, key.status);
        assertNotNull(key.kid);
        assertEquals(32, key.kid.length()); // KID de 32 caracteres
        assertNotNull(key.publicKeyPem);
        assertNotNull(key.privateKeyCiphertext);
        assertNotNull(key.createdAt);
        assertNull(key.updatedAt); // Não deve ter updatedAt na criação
        assertNull(key.deletedAt);
        assertNull(key.rotatedAt);
        assertNull(key.expiresAt);
    }

    @Test
    @Transactional
    void testOnStart_GeneratesUniqueKid() {
        // Arrange
        StartupEvent ev1 = new StartupEvent();
        StartupEvent ev2 = new StartupEvent();

        // Act - Criar primeira chave
        jwtKeySeeder.onStart(ev1);
        JwtKey key1 = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        String kid1 = key1.kid;
        
        // Deletar e criar novamente
        JwtKey.deleteAll();
        jwtKeySeeder.onStart(ev2);
        JwtKey key2 = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        String kid2 = key2.kid;

        // Assert
        assertNotEquals(kid1, kid2); // KIDs devem ser diferentes
        assertEquals(32, kid1.length());
        assertEquals(32, kid2.length());
    }

    @Test
    @Transactional
    void testOnStart_DoesNotCreate_WhenRetiredKeyExists() {
        // Arrange
        // Criar chave RETIRED (não ativa)
        JwtKey retiredKey = new JwtKey();
        retiredKey.kid = "retired-key-id-1234567890123456";
        retiredKey.alg = JwtAlg.RS256;
        retiredKey.publicKeyPem = "-----BEGIN PUBLIC KEY-----\ntest\n-----END PUBLIC KEY-----";
        retiredKey.privateKeyCiphertext = "-----BEGIN PRIVATE KEY-----\ntest\n-----END PRIVATE KEY-----";
        retiredKey.status = KeyStatus.RETIRED;
        retiredKey.persist();

        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);

        // Assert
        long activeCount = JwtKey.count("status = ?1", KeyStatus.ACTIVE);
        assertEquals(1, activeCount); // Deve criar uma nova chave ativa
        
        long retiredCount = JwtKey.count("status = ?1", KeyStatus.RETIRED);
        assertEquals(1, retiredCount); // Chave retirada mantida
    }

    @Test
    @Transactional
    void testOnStart_PemFormatIsCorrect() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        jwtKeySeeder.onStart(ev);

        // Assert
        JwtKey key = JwtKey.find("status = ?1", KeyStatus.ACTIVE).firstResult();
        assertNotNull(key);
        
        // Verificar formato PEM da chave pública
        String publicKeyPem = key.publicKeyPem;
        assertTrue(publicKeyPem.contains("-----BEGIN PUBLIC KEY-----"));
        assertTrue(publicKeyPem.contains("-----END PUBLIC KEY-----"));
        // Verificar que não há linhas muito longas (chunking de 64 caracteres)
        String[] lines = publicKeyPem.split("\n");
        for (String line : lines) {
            if (!line.startsWith("-----")) {
                assertTrue(line.length() <= 64, "Linha PEM não deve ter mais de 64 caracteres: " + line);
            }
        }
        
        // Verificar formato PEM da chave privada
        String privateKeyPem = key.privateKeyCiphertext;
        assertTrue(privateKeyPem.contains("-----BEGIN PRIVATE KEY-----"));
        assertTrue(privateKeyPem.contains("-----END PRIVATE KEY-----"));
        // Verificar chunking
        String[] privateLines = privateKeyPem.split("\n");
        for (String line : privateLines) {
            if (!line.startsWith("-----")) {
                assertTrue(line.length() <= 64, "Linha PEM não deve ter mais de 64 caracteres: " + line);
            }
        }
    }
}

