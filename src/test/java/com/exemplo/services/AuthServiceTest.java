package com.exemplo.services;

import com.exemplo.entities.AdminUser;
import com.exemplo.entities.ApiClient;
import com.exemplo.entities.JwtKey;
import com.exemplo.entities.Session;
import com.exemplo.enums.JwtAlg;
import com.exemplo.enums.KeyStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService authService;

    @Inject
    SessionService sessionService;

    @Inject
    JwtKeyService jwtKeyService;

    private ApiClient testClient;
    private AdminUser testAdmin;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Limpar dados de teste anteriores (ordem importa devido a foreign keys)
        com.exemplo.entities.ApiAccessLog.deleteAll();
        Session.deleteAll();
        ApiClient.delete("email = ?1", "test-client@test.com");
        AdminUser.delete("email = ?1", "test-admin@test.com");
        
        // Limpar e criar chave JWT válida (necessária para issueToken)
        JwtKey.deleteAll();
        createValidJwtKey();

        // Criar cliente de teste
        testClient = new ApiClient();
        testClient.name = "Test Client";
        testClient.clientId = "test-client-id";
        testClient.email = "test-client@test.com";
        testClient.clientSecret = "test-password";
        testClient.isActive = true;
        testClient.tokenVersion = 1;
        testClient.persist();

        // Criar admin de teste
        testAdmin = new AdminUser();
        testAdmin.name = "Test Admin";
        testAdmin.username = "testadmin";
        testAdmin.email = "test-admin@test.com";
        testAdmin.password = "admin-password";
        testAdmin.isActive = true;
        testAdmin.deletedAt = null;
        testAdmin.persist();
        
        // Atualizar cache do JwtKeyService
        jwtKeyService.warmUp();
    }
    
    private void createValidJwtKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        JwtKey jwtKey = new JwtKey();
        jwtKey.kid = "test-auth-key-" + System.currentTimeMillis();
        jwtKey.alg = JwtAlg.RS256;
        jwtKey.publicKeyPem = toPem(keyPair.getPublic());
        jwtKey.privateKeyCiphertext = toPem(keyPair.getPrivate());
        jwtKey.status = KeyStatus.ACTIVE;
        jwtKey.createdAt = LocalDateTime.now();
        jwtKey.persist();
    }
    
    private String toPem(PublicKey key) {
        byte[] encoded = key.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        String header = key instanceof RSAPublicKey 
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

    @Test
    @Transactional
    void testIssueTokenForClient_Success() {
        // Arrange
        String email = "test-client@test.com";
        String password = "test-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForClient(email, password, ipAddress, userAgent);

        // Assert
        assertNotNull(jwt);
        assertFalse(jwt.isEmpty());
        assertTrue(jwt.contains(".")); // JWT tem formato header.payload.signature

        // Verificar que uma sessão foi criada
        Session session = Session.find("tokenJti is not null").firstResult();
        assertNotNull(session);
        assertEquals(testClient.id, session.apiClient.id);
        assertEquals(ipAddress, session.ipAddress);
        assertEquals(userAgent, session.userAgent);
        assertTrue(session.isActive);
    }

    @Test
    @Transactional
    void testIssueTokenForClient_ClientNotFound() {
        // Arrange
        String email = "nonexistent@test.com";
        String password = "password123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForClient(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForClient_InvalidPassword() {
        // Arrange
        String email = "test-client@test.com";
        String password = "wrongpassword";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForClient(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForClient_InactiveClient() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        client.isActive = false;
        client.persist();

        String email = "test-client@test.com";
        String password = "test-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForClient(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForAdmin_Success() {
        // Arrange
        String email = "test-admin@test.com";
        String password = "admin-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        LocalDateTime beforeLogin = LocalDateTime.now();

        // Act
        String jwt = authService.issueTokenForAdmin(email, password, ipAddress, userAgent);

        // Assert
        assertNotNull(jwt);
        assertFalse(jwt.isEmpty());
        assertTrue(jwt.contains(".")); // JWT tem formato header.payload.signature

        // Verificar que uma sessão foi criada
        Session session = Session.find("adminUser.id = ?1", testAdmin.id).firstResult();
        assertNotNull(session);
        assertEquals(testAdmin.id, session.adminUser.id);
        assertEquals(ipAddress, session.ipAddress);
        assertEquals(userAgent, session.userAgent);
        assertTrue(session.isActive);

        // Verificar que lastLoginAt foi atualizado
        AdminUser admin = AdminUser.findById(testAdmin.id);
        assertNotNull(admin.lastLoginAt);
        assertTrue(admin.lastLoginAt.isAfter(beforeLogin) || admin.lastLoginAt.isEqual(beforeLogin));
    }

    @Test
    @Transactional
    void testIssueTokenForAdmin_AdminNotFound() {
        // Arrange
        String email = "nonexistent@test.com";
        String password = "admin123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForAdmin(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForAdmin_InvalidPassword() {
        // Arrange
        String email = "test-admin@test.com";
        String password = "wrongpassword";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForAdmin(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForAdmin_InactiveAdmin() {
        // Arrange
        AdminUser admin = AdminUser.findById(testAdmin.id);
        admin.isActive = false;
        admin.persist();

        String email = "test-admin@test.com";
        String password = "admin-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForAdmin(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForAdmin_DeletedAdmin() {
        // Arrange
        AdminUser admin = AdminUser.findById(testAdmin.id);
        admin.deletedAt = LocalDateTime.now();
        admin.persist();

        String email = "test-admin@test.com";
        String password = "admin-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForAdmin(email, password, ipAddress, userAgent);

        // Assert
        assertNull(jwt);
    }

    @Test
    @Transactional
    void testIssueTokenForClient_TokenExpiration() {
        // Arrange
        String email = "test-client@test.com";
        String password = "test-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForClient(email, password, ipAddress, userAgent);

        // Assert
        assertNotNull(jwt);
        
        // Verificar que a sessão tem expiração de 10 minutos
        Session session = Session.find("apiClient.id = ?1", testClient.id).firstResult();
        assertNotNull(session);
        assertNotNull(session.expiresAt);
        // A expiração deve ser aproximadamente 10 minutos no futuro
        LocalDateTime expectedExpiration = LocalDateTime.now().plusMinutes(10);
        assertTrue(session.expiresAt.isBefore(expectedExpiration.plusSeconds(30)) && 
                   session.expiresAt.isAfter(expectedExpiration.minusSeconds(30)));
    }

    @Test
    @Transactional
    void testIssueTokenForAdmin_TokenExpiration() {
        // Arrange
        String email = "test-admin@test.com";
        String password = "admin-password";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Act
        String jwt = authService.issueTokenForAdmin(email, password, ipAddress, userAgent);

        // Assert
        assertNotNull(jwt);
        
        // Verificar que a sessão tem expiração de 8 horas
        Session session = Session.find("adminUser.id = ?1", testAdmin.id).firstResult();
        assertNotNull(session);
        assertNotNull(session.expiresAt);
        // A expiração deve ser aproximadamente 8 horas no futuro
        LocalDateTime expectedExpiration = LocalDateTime.now().plusHours(8);
        assertTrue(session.expiresAt.isBefore(expectedExpiration.plusMinutes(1)) && 
                   session.expiresAt.isAfter(expectedExpiration.minusMinutes(1)));
    }
}
