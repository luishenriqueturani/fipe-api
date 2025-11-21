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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SessionServiceTest {

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
    void testCreateSessionForClient_Success() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        Session session = sessionService.createSessionForClient(client, ipAddress, userAgent, expiresAt);

        // Assert
        assertNotNull(session);
        assertNotNull(session.id);
        assertEquals(client.id, session.apiClient.id);
        assertNull(session.adminUser);
        assertNotNull(session.tokenJti);
        assertFalse(session.tokenJti.isEmpty());
        assertEquals(ipAddress, session.ipAddress);
        assertEquals(userAgent, session.userAgent);
        assertEquals(expiresAt, session.expiresAt);
        assertTrue(session.isActive);
        assertNull(session.logoutAt);
        assertNotNull(session.loginAt);
        assertTrue(session.loginAt.isAfter(beforeCreation.minusSeconds(1)) || 
                   session.loginAt.isEqual(beforeCreation));
        assertNotNull(session.lastActivityAt);
        assertTrue(session.lastActivityAt.isAfter(beforeCreation.minusSeconds(1)) || 
                   session.lastActivityAt.isEqual(beforeCreation));

        // Verificar que lastAccessAt do cliente foi atualizado
        ApiClient updatedClient = ApiClient.findById(testClient.id);
        assertNotNull(updatedClient.lastAccessAt);
        assertTrue(updatedClient.lastAccessAt.isAfter(beforeCreation.minusSeconds(1)) || 
                   updatedClient.lastAccessAt.isEqual(beforeCreation));
    }

    @Test
    @Transactional
    void testCreateSessionForAdmin_Success() {
        // Arrange
        AdminUser admin = AdminUser.findById(testAdmin.id);
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);
        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        Session session = sessionService.createSessionForAdmin(admin, ipAddress, userAgent, expiresAt);

        // Assert
        assertNotNull(session);
        assertNotNull(session.id);
        assertEquals(admin.id, session.adminUser.id);
        assertNull(session.apiClient);
        assertNotNull(session.tokenJti);
        assertFalse(session.tokenJti.isEmpty());
        assertEquals(ipAddress, session.ipAddress);
        assertEquals(userAgent, session.userAgent);
        assertEquals(expiresAt, session.expiresAt);
        assertTrue(session.isActive);
        assertNull(session.logoutAt);
        assertNotNull(session.loginAt);
        assertTrue(session.loginAt.isAfter(beforeCreation.minusSeconds(1)) || 
                   session.loginAt.isEqual(beforeCreation));
        assertNotNull(session.lastActivityAt);
        assertTrue(session.lastActivityAt.isAfter(beforeCreation.minusSeconds(1)) || 
                   session.lastActivityAt.isEqual(beforeCreation));
    }

    @Test
    @Transactional
    void testUpdateLastActivity_Success() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session session = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        String tokenJti = session.tokenJti;
        LocalDateTime originalLastActivity = session.lastActivityAt;

        // Aguardar um pouco para garantir diferença de tempo
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        sessionService.updateLastActivity(tokenJti);

        // Assert
        Session updatedSession = Session.findById(session.id);
        assertNotNull(updatedSession);
        assertTrue(updatedSession.lastActivityAt.isAfter(originalLastActivity));
    }

    @Test
    @Transactional
    void testUpdateLastActivity_ExpiredSession() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session session = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().minusHours(1)); // Sessão expirada
        String tokenJti = session.tokenJti;
        LocalDateTime originalLastActivity = session.lastActivityAt;

        // Act
        sessionService.updateLastActivity(tokenJti);

        // Assert - Não deve atualizar porque a sessão está expirada
        Session updatedSession = Session.findById(session.id);
        assertNotNull(updatedSession);
        assertEquals(originalLastActivity, updatedSession.lastActivityAt);
    }

    @Test
    @Transactional
    void testUpdateLastActivity_InactiveSession() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session session = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        session.isActive = false;
        session.persist();
        String tokenJti = session.tokenJti;
        LocalDateTime originalLastActivity = session.lastActivityAt;

        // Act
        sessionService.updateLastActivity(tokenJti);

        // Assert - Não deve atualizar porque a sessão está inativa
        Session updatedSession = Session.findById(session.id);
        assertNotNull(updatedSession);
        assertEquals(originalLastActivity, updatedSession.lastActivityAt);
    }

    @Test
    @Transactional
    void testUpdateLastActivity_NonExistentTokenJti() {
        // Arrange
        String nonExistentTokenJti = "non-existent-jti";

        // Act - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            sessionService.updateLastActivity(nonExistentTokenJti);
        });
    }

    @Test
    @Transactional
    void testLogoutSession_Success() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session session = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        String tokenJti = session.tokenJti;
        LocalDateTime beforeLogout = LocalDateTime.now();

        // Act
        sessionService.logoutSession(tokenJti);

        // Assert
        Session loggedOutSession = Session.findById(session.id);
        assertNotNull(loggedOutSession);
        assertFalse(loggedOutSession.isActive);
        assertNotNull(loggedOutSession.logoutAt);
        assertTrue(loggedOutSession.logoutAt.isAfter(beforeLogout.minusSeconds(1)) || 
                   loggedOutSession.logoutAt.isEqual(beforeLogout));
    }

    @Test
    @Transactional
    void testLogoutSession_NonExistentTokenJti() {
        // Arrange
        String nonExistentTokenJti = "non-existent-jti";

        // Act - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            sessionService.logoutSession(nonExistentTokenJti);
        });
    }

    @Test
    @Transactional
    void testGetActiveSessions_Success() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        AdminUser admin = AdminUser.findById(testAdmin.id);
        
        Session activeSession1 = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        Session activeSession2 = sessionService.createSessionForAdmin(
                admin, "192.168.1.2", "Chrome", LocalDateTime.now().plusHours(8));
        
        // Criar sessão expirada
        Session expiredSession = sessionService.createSessionForClient(
                client, "192.168.1.3", "Firefox", LocalDateTime.now().minusHours(1));
        
        // Criar sessão inativa
        Session inactiveSession = sessionService.createSessionForClient(
                client, "192.168.1.4", "Safari", LocalDateTime.now().plusHours(1));
        inactiveSession.isActive = false;
        inactiveSession.persist();

        // Act
        List<Session> activeSessions = sessionService.getActiveSessions();

        // Assert
        assertNotNull(activeSessions);
        assertEquals(2, activeSessions.size());
        assertTrue(activeSessions.stream().anyMatch(s -> s.id.equals(activeSession1.id)));
        assertTrue(activeSessions.stream().anyMatch(s -> s.id.equals(activeSession2.id)));
        assertFalse(activeSessions.stream().anyMatch(s -> s.id.equals(expiredSession.id)));
        assertFalse(activeSessions.stream().anyMatch(s -> s.id.equals(inactiveSession.id)));
    }

    @Test
    @Transactional
    void testGetActiveSessions_Empty() {
        // Act
        List<Session> activeSessions = sessionService.getActiveSessions();

        // Assert
        assertNotNull(activeSessions);
        assertTrue(activeSessions.isEmpty());
    }

    @Test
    @Transactional
    void testGetActiveSessionsForClient_Success() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        
        // Limpar cliente anterior se existir
        ApiClient.delete("clientId = ?1", "another-client-id");
        
        ApiClient anotherClient = new ApiClient();
        anotherClient.name = "Another Client";
        anotherClient.clientId = "another-client-id";
        anotherClient.email = "another@test.com";
        anotherClient.clientSecret = "password";
        anotherClient.isActive = true;
        anotherClient.tokenVersion = 1;
        anotherClient.persist();

        Session session1 = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        Session session2 = sessionService.createSessionForClient(
                client, "192.168.1.2", "Chrome", LocalDateTime.now().plusHours(1));
        Session session3 = sessionService.createSessionForClient(
                anotherClient, "192.168.1.3", "Firefox", LocalDateTime.now().plusHours(1));

        // Act
        List<Session> clientSessions = sessionService.getActiveSessionsForClient(testClient.id);

        // Assert
        assertNotNull(clientSessions);
        assertEquals(2, clientSessions.size());
        assertTrue(clientSessions.stream().anyMatch(s -> s.id.equals(session1.id)));
        assertTrue(clientSessions.stream().anyMatch(s -> s.id.equals(session2.id)));
        assertFalse(clientSessions.stream().anyMatch(s -> s.id.equals(session3.id)));
    }

    @Test
    @Transactional
    void testGetActiveSessionsForClient_WithExpiredSessions() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session activeSession = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        Session expiredSession = sessionService.createSessionForClient(
                client, "192.168.1.2", "Chrome", LocalDateTime.now().minusHours(1));

        // Act
        List<Session> clientSessions = sessionService.getActiveSessionsForClient(testClient.id);

        // Assert
        assertNotNull(clientSessions);
        assertEquals(1, clientSessions.size());
        assertEquals(activeSession.id, clientSessions.get(0).id);
    }

    @Test
    @Transactional
    void testGetActiveSessionsForAdmin_Success() {
        // Arrange
        AdminUser admin = AdminUser.findById(testAdmin.id);
        
        // Limpar admin anterior se existir
        AdminUser.delete("username = ?1", "anotheradmin");
        
        AdminUser anotherAdmin = new AdminUser();
        anotherAdmin.name = "Another Admin";
        anotherAdmin.username = "anotheradmin";
        anotherAdmin.email = "another-admin@test.com";
        anotherAdmin.password = "password";
        anotherAdmin.isActive = true;
        anotherAdmin.deletedAt = null;
        anotherAdmin.persist();

        Session session1 = sessionService.createSessionForAdmin(
                admin, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(8));
        Session session2 = sessionService.createSessionForAdmin(
                admin, "192.168.1.2", "Chrome", LocalDateTime.now().plusHours(8));
        Session session3 = sessionService.createSessionForAdmin(
                anotherAdmin, "192.168.1.3", "Firefox", LocalDateTime.now().plusHours(8));

        // Act
        List<Session> adminSessions = sessionService.getActiveSessionsForAdmin(testAdmin.id);

        // Assert
        assertNotNull(adminSessions);
        assertEquals(2, adminSessions.size());
        assertTrue(adminSessions.stream().anyMatch(s -> s.id.equals(session1.id)));
        assertTrue(adminSessions.stream().anyMatch(s -> s.id.equals(session2.id)));
        assertFalse(adminSessions.stream().anyMatch(s -> s.id.equals(session3.id)));
    }

    @Test
    @Transactional
    void testGetActiveSessionsForAdmin_WithExpiredSessions() {
        // Arrange
        AdminUser admin = AdminUser.findById(testAdmin.id);
        Session activeSession = sessionService.createSessionForAdmin(
                admin, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(8));
        Session expiredSession = sessionService.createSessionForAdmin(
                admin, "192.168.1.2", "Chrome", LocalDateTime.now().minusHours(1));

        // Act
        List<Session> adminSessions = sessionService.getActiveSessionsForAdmin(testAdmin.id);

        // Assert
        assertNotNull(adminSessions);
        assertEquals(1, adminSessions.size());
        assertEquals(activeSession.id, adminSessions.get(0).id);
    }

    @Test
    @Transactional
    void testFindByTokenJti_Success() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session session = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        String tokenJti = session.tokenJti;

        // Act
        Session foundSession = sessionService.findByTokenJti(tokenJti);

        // Assert
        assertNotNull(foundSession);
        assertEquals(session.id, foundSession.id);
        assertEquals(tokenJti, foundSession.tokenJti);
    }

    @Test
    @Transactional
    void testFindByTokenJti_NotFound() {
        // Arrange
        String nonExistentTokenJti = "non-existent-jti";

        // Act
        Session foundSession = sessionService.findByTokenJti(nonExistentTokenJti);

        // Assert
        assertNull(foundSession);
    }

    @Test
    @Transactional
    void testCreateSessionForClient_UniqueTokenJti() {
        // Arrange
        ApiClient client = ApiClient.findById(testClient.id);
        Session session1 = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        Session session2 = sessionService.createSessionForClient(
                client, "192.168.1.2", "Chrome", LocalDateTime.now().plusHours(1));

        // Assert
        assertNotEquals(session1.tokenJti, session2.tokenJti);
    }

    @Test
    @Transactional
    void testCreateSessionForAdmin_UniqueTokenJti() {
        // Arrange
        AdminUser admin = AdminUser.findById(testAdmin.id);
        Session session1 = sessionService.createSessionForAdmin(
                admin, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(8));
        Session session2 = sessionService.createSessionForAdmin(
                admin, "192.168.1.2", "Chrome", LocalDateTime.now().plusHours(8));

        // Assert
        assertNotEquals(session1.tokenJti, session2.tokenJti);
    }
}

