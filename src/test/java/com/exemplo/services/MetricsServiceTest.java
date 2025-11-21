package com.exemplo.services;

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
class MetricsServiceTest {

    @Inject
    MetricsService metricsService;

    private ApiClient testClient;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste
        Session.deleteAll();
        ApiClient.deleteAll();

        // Criar cliente de teste
        testClient = new ApiClient();
        testClient.name = "Test Client";
        testClient.clientId = "test-client-metrics";
        testClient.email = "test-metrics@test.com";
        testClient.clientSecret = "test-secret";
        testClient.isActive = true;
        testClient.tokenVersion = 1;
        testClient.persist();
    }

    // ========== Counter Tests ==========

    @Test
    @Transactional
    void testIncrementTokensIssued() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.incrementTokensIssued();
        });
    }

    @Test
    @Transactional
    void testIncrementAdminTokensIssued() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.incrementAdminTokensIssued();
        });
    }

    @Test
    @Transactional
    void testIncrementSuccessfulLogins() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.incrementSuccessfulLogins();
        });
    }

    @Test
    @Transactional
    void testIncrementFailedLogins() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.incrementFailedLogins();
        });
    }

    @Test
    @Transactional
    void testIncrementDataUpdates() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.incrementDataUpdates();
        });
    }

    @Test
    @Transactional
    void testIncrementSearches() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.incrementSearches();
        });
    }

    // ========== Gauge Tests ==========

    @Test
    @Transactional
    void testGetActiveSessionsCount_NoActiveSessions() {
        // Act
        long count = metricsService.getActiveSessionsCount();

        // Assert
        assertEquals(0, count);
    }

    @Test
    @Transactional
    void testGetActiveSessionsCount_WithActiveSessions() {
        // Arrange - Criar sessões ativas
        Session session1 = new Session();
        session1.apiClient = testClient;
        session1.tokenJti = "jti-1";
        session1.ipAddress = "127.0.0.1";
        session1.userAgent = "TestAgent";
        session1.isActive = true;
        session1.expiresAt = LocalDateTime.now().plusHours(1);
        session1.loginAt = LocalDateTime.now();
        session1.lastActivityAt = LocalDateTime.now();
        session1.persist();

        Session session2 = new Session();
        session2.apiClient = testClient;
        session2.tokenJti = "jti-2";
        session2.ipAddress = "127.0.0.1";
        session2.userAgent = "TestAgent";
        session2.isActive = true;
        session2.expiresAt = LocalDateTime.now().plusHours(2);
        session2.loginAt = LocalDateTime.now();
        session2.lastActivityAt = LocalDateTime.now();
        session2.persist();

        // Act
        long count = metricsService.getActiveSessionsCount();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @Transactional
    void testGetActiveSessionsCount_ExcludesInactiveSessions() {
        // Arrange - Criar sessão ativa e inativa
        Session activeSession = new Session();
        activeSession.apiClient = testClient;
        activeSession.tokenJti = "jti-active";
        activeSession.ipAddress = "127.0.0.1";
        activeSession.userAgent = "TestAgent";
        activeSession.isActive = true;
        activeSession.expiresAt = LocalDateTime.now().plusHours(1);
        activeSession.loginAt = LocalDateTime.now();
        activeSession.lastActivityAt = LocalDateTime.now();
        activeSession.persist();

        Session inactiveSession = new Session();
        inactiveSession.apiClient = testClient;
        inactiveSession.tokenJti = "jti-inactive";
        inactiveSession.ipAddress = "127.0.0.1";
        inactiveSession.userAgent = "TestAgent";
        inactiveSession.isActive = false;
        inactiveSession.expiresAt = LocalDateTime.now().plusHours(1);
        inactiveSession.loginAt = LocalDateTime.now();
        inactiveSession.lastActivityAt = LocalDateTime.now();
        inactiveSession.persist();

        // Act
        long count = metricsService.getActiveSessionsCount();

        // Assert
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void testGetActiveSessionsCount_ExcludesExpiredSessions() {
        // Arrange - Criar sessão ativa não expirada e expirada
        Session activeSession = new Session();
        activeSession.apiClient = testClient;
        activeSession.tokenJti = "jti-active";
        activeSession.ipAddress = "127.0.0.1";
        activeSession.userAgent = "TestAgent";
        activeSession.isActive = true;
        activeSession.expiresAt = LocalDateTime.now().plusHours(1);
        activeSession.loginAt = LocalDateTime.now();
        activeSession.lastActivityAt = LocalDateTime.now();
        activeSession.persist();

        Session expiredSession = new Session();
        expiredSession.apiClient = testClient;
        expiredSession.tokenJti = "jti-expired";
        expiredSession.ipAddress = "127.0.0.1";
        expiredSession.userAgent = "TestAgent";
        expiredSession.isActive = true;
        expiredSession.expiresAt = LocalDateTime.now().minusHours(1); // Expirada
        expiredSession.loginAt = LocalDateTime.now().minusHours(2);
        expiredSession.lastActivityAt = LocalDateTime.now().minusHours(2);
        expiredSession.persist();

        // Act
        long count = metricsService.getActiveSessionsCount();

        // Assert
        assertEquals(1, count);
    }

    // ========== Timer Tests ==========

    @Test
    @Transactional
    void testTimeAuthOperation() {
        // Act - Chamar o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            metricsService.timeAuthOperation();
        });
    }

    // ========== Multiple Calls Tests ==========

    @Test
    @Transactional
    void testMultipleMetricCalls() {
        // Act - Chamar múltiplos métodos de métricas
        assertDoesNotThrow(() -> {
            metricsService.incrementTokensIssued();
            metricsService.incrementAdminTokensIssued();
            metricsService.incrementSuccessfulLogins();
            metricsService.incrementFailedLogins();
            metricsService.incrementDataUpdates();
            metricsService.incrementSearches();
            metricsService.timeAuthOperation();
            long count = metricsService.getActiveSessionsCount();
            assertTrue(count >= 0);
        });
    }
}

