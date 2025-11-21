package com.exemplo.services;

import com.exemplo.entities.AdminUser;
import com.exemplo.entities.ApiClient;
import com.exemplo.entities.Session;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService authService;

    @Inject
    SessionService sessionService;

    private ApiClient testClient;
    private AdminUser testAdmin;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste anteriores (ordem importa devido a foreign keys)
        com.exemplo.entities.ApiAccessLog.deleteAll();
        Session.deleteAll();
        ApiClient.delete("email = ?1", "test-client@test.com");
        AdminUser.delete("email = ?1", "test-admin@test.com");

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
