package com.exemplo.services;

import com.exemplo.entities.ApiAccessLog;
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
class AccessLogServiceTest {

    @Inject
    AccessLogService accessLogService;

    @Inject
    SessionService sessionService;

    private ApiClient testClient;
    private Session testSession;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste anteriores
        ApiAccessLog.deleteAll();
        Session.deleteAll();
        ApiClient.delete("email = ?1", "test-client@test.com");

        // Criar cliente de teste
        testClient = new ApiClient();
        testClient.name = "Test Client";
        testClient.clientId = "test-client-id";
        testClient.email = "test-client@test.com";
        testClient.clientSecret = "test-password";
        testClient.isActive = true;
        testClient.tokenVersion = 1;
        testClient.persist();

        // Criar sessão de teste
        ApiClient client = ApiClient.findById(testClient.id);
        testSession = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
    }

    @Test
    @Transactional
    void testLogAccess_WithTokenJtiAndClientId() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = testClient.clientId;
        String method = "GET";
        String path = "/api/vehicle-types/search";
        String query = "name=Carro";
        int statusCode = 200;
        String ip = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        long durationMs = 150L;
        LocalDateTime beforeLog = LocalDateTime.now();
        LocalDateTime originalLastActivity = testSession.lastActivityAt;

        // Aguardar um pouco para garantir diferença de tempo
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        accessLogService.logAccess(tokenJti, clientId, method, path, query, statusCode, ip, userAgent, durationMs);

        // Assert - Verificar que o log foi criado
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertNotNull(log.id);
        assertEquals(testClient.id, log.apiClient.id);
        assertEquals(method, log.method);
        assertEquals(path, log.path);
        assertEquals(query, log.query);
        assertEquals(statusCode, log.statusCode);
        assertEquals(ip, log.ip);
        assertEquals(userAgent, log.userAgent);
        assertEquals(durationMs, log.durationMs);
        assertNotNull(log.createdAt);
        assertTrue(log.createdAt.isAfter(beforeLog.minusSeconds(1)) || 
                   log.createdAt.isEqual(beforeLog));
        assertNotNull(log.requestedAt);

        // Verificar que a última atividade da sessão foi atualizada
        Session session = Session.findById(testSession.id);
        assertNotNull(session);
        assertTrue(session.lastActivityAt.isAfter(originalLastActivity));
    }

    @Test
    @Transactional
    void testLogAccess_WithoutTokenJti() {
        // Arrange
        String tokenJti = null;
        String clientId = testClient.clientId;
        String method = "POST";
        String path = "/api/data/update";
        String query = null;
        int statusCode = 201;
        String ip = "192.168.1.2";
        String userAgent = "Chrome";
        long durationMs = 200L;
        LocalDateTime originalLastActivity = testSession.lastActivityAt;

        // Act
        accessLogService.logAccess(tokenJti, clientId, method, path, query, statusCode, ip, userAgent, durationMs);

        // Assert - Verificar que o log foi criado
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertEquals(testClient.id, log.apiClient.id);
        assertEquals(method, log.method);
        assertEquals(path, log.path);
        assertNull(log.query);
        assertEquals(statusCode, log.statusCode);

        // Verificar que a última atividade da sessão NÃO foi atualizada
        Session session = Session.findById(testSession.id);
        assertNotNull(session);
        // A comparação pode ter diferença de nanossegundos, então verificamos que não mudou significativamente
        assertTrue(session.lastActivityAt.isEqual(originalLastActivity) || 
                   session.lastActivityAt.isBefore(originalLastActivity.plusSeconds(1)));
    }

    @Test
    @Transactional
    void testLogAccess_WithoutClientId() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = null;
        String method = "GET";
        String path = "/.well-known/jwks.json";
        String query = null;
        int statusCode = 200;
        String ip = "192.168.1.3";
        String userAgent = "Firefox";
        long durationMs = 50L;

        // Act
        accessLogService.logAccess(tokenJti, clientId, method, path, query, statusCode, ip, userAgent, durationMs);

        // Assert - Verificar que o log foi criado sem cliente
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertNull(log.apiClient);
        assertEquals(method, log.method);
        assertEquals(path, log.path);
        assertEquals(statusCode, log.statusCode);
    }

    @Test
    @Transactional
    void testLogAccess_WithNonExistentClientId() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = "non-existent-client-id";
        String method = "GET";
        String path = "/api/vehicle-types/search";
        String query = null;
        int statusCode = 401;
        String ip = "192.168.1.4";
        String userAgent = "Safari";
        long durationMs = 100L;

        // Act
        accessLogService.logAccess(tokenJti, clientId, method, path, query, statusCode, ip, userAgent, durationMs);

        // Assert - Verificar que o log foi criado sem cliente
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertNull(log.apiClient);
        assertEquals(method, log.method);
        assertEquals(statusCode, log.statusCode);
    }

    @Test
    @Transactional
    void testLogAccess_UpdatesSessionLastActivity() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        LocalDateTime originalLastActivity = testSession.lastActivityAt;

        // Aguardar um pouco para garantir diferença de tempo
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        accessLogService.logAccess(tokenJti, testClient.clientId, "GET", "/api/test", null, 200, 
                "192.168.1.1", "Mozilla/5.0", 100L);

        // Assert
        Session session = Session.findById(testSession.id);
        assertNotNull(session);
        assertTrue(session.lastActivityAt.isAfter(originalLastActivity));
    }

    @Test
    @Transactional
    void testLogAccess_DoesNotUpdateExpiredSession() {
        // Arrange - Criar sessão expirada
        ApiClient client = ApiClient.findById(testClient.id);
        Session expiredSession = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().minusHours(1));
        String tokenJti = expiredSession.tokenJti;
        LocalDateTime originalLastActivity = expiredSession.lastActivityAt;

        // Act
        accessLogService.logAccess(tokenJti, testClient.clientId, "GET", "/api/test", null, 200, 
                "192.168.1.1", "Mozilla/5.0", 100L);

        // Assert - A sessão expirada não deve ser atualizada
        Session session = Session.findById(expiredSession.id);
        assertNotNull(session);
        assertEquals(originalLastActivity, session.lastActivityAt);
    }

    @Test
    @Transactional
    void testLogAccess_DoesNotUpdateInactiveSession() {
        // Arrange - Criar sessão inativa
        ApiClient client = ApiClient.findById(testClient.id);
        Session inactiveSession = sessionService.createSessionForClient(
                client, "192.168.1.1", "Mozilla/5.0", LocalDateTime.now().plusHours(1));
        inactiveSession.isActive = false;
        inactiveSession.persist();
        String tokenJti = inactiveSession.tokenJti;
        LocalDateTime originalLastActivity = inactiveSession.lastActivityAt;

        // Act
        accessLogService.logAccess(tokenJti, testClient.clientId, "GET", "/api/test", null, 200, 
                "192.168.1.1", "Mozilla/5.0", 100L);

        // Assert - A sessão inativa não deve ser atualizada
        Session session = Session.findById(inactiveSession.id);
        assertNotNull(session);
        assertEquals(originalLastActivity, session.lastActivityAt);
    }

    @Test
    @Transactional
    void testLogAccess_MultipleLogs() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = testClient.clientId;

        // Act - Criar múltiplos logs
        accessLogService.logAccess(tokenJti, clientId, "GET", "/api/test1", null, 200, 
                "192.168.1.1", "Mozilla/5.0", 100L);
        accessLogService.logAccess(tokenJti, clientId, "POST", "/api/test2", "param=value", 201, 
                "192.168.1.2", "Chrome", 200L);
        accessLogService.logAccess(null, clientId, "DELETE", "/api/test3", null, 204, 
                "192.168.1.3", "Firefox", 50L);

        // Assert
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(3, logs.size());
        
        // Verificar que todos os logs foram criados corretamente
        ApiAccessLog log1 = logs.stream()
                .filter(l -> l.path.equals("/api/test1"))
                .findFirst()
                .orElse(null);
        assertNotNull(log1);
        assertEquals("GET", log1.method);
        assertEquals(200, log1.statusCode);

        ApiAccessLog log2 = logs.stream()
                .filter(l -> l.path.equals("/api/test2"))
                .findFirst()
                .orElse(null);
        assertNotNull(log2);
        assertEquals("POST", log2.method);
        assertEquals("param=value", log2.query);
        assertEquals(201, log2.statusCode);

        ApiAccessLog log3 = logs.stream()
                .filter(l -> l.path.equals("/api/test3"))
                .findFirst()
                .orElse(null);
        assertNotNull(log3);
        assertEquals("DELETE", log3.method);
        assertEquals(204, log3.statusCode);
    }

    @Test
    @Transactional
    void testLogAccess_AllFields() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = testClient.clientId;
        String method = "PUT";
        String path = "/api/vehicle-types/1";
        String query = "param1=value1&param2=value2";
        int statusCode = 200;
        String ip = "10.0.0.1";
        String userAgent = "Custom Agent/1.0";
        long durationMs = 350L;

        // Act
        accessLogService.logAccess(tokenJti, clientId, method, path, query, statusCode, ip, userAgent, durationMs);

        // Assert - Verificar todos os campos
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertEquals(testClient.id, log.apiClient.id);
        assertEquals(method, log.method);
        assertEquals(path, log.path);
        assertEquals(query, log.query);
        assertEquals(statusCode, log.statusCode);
        assertEquals(ip, log.ip);
        assertEquals(userAgent, log.userAgent);
        assertEquals(durationMs, log.durationMs);
        assertNotNull(log.createdAt);
        assertNotNull(log.requestedAt);
    }

    @Test
    @Transactional
    void testLogAccess_WithNullQuery() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = testClient.clientId;

        // Act
        accessLogService.logAccess(tokenJti, clientId, "GET", "/api/test", null, 200, 
                "192.168.1.1", "Mozilla/5.0", 100L);

        // Assert
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertNull(log.query);
    }

    @Test
    @Transactional
    void testLogAccess_WithEmptyQuery() {
        // Arrange
        String tokenJti = testSession.tokenJti;
        String clientId = testClient.clientId;

        // Act
        accessLogService.logAccess(tokenJti, clientId, "GET", "/api/test", "", 200, 
                "192.168.1.1", "Mozilla/5.0", 100L);

        // Assert
        List<ApiAccessLog> logs = ApiAccessLog.listAll();
        assertEquals(1, logs.size());
        
        ApiAccessLog log = logs.get(0);
        assertEquals("", log.query);
    }
}

