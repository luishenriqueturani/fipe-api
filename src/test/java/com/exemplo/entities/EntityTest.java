package com.exemplo.entities;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EntityTest {

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste
        ApiAccessLog.deleteAll();
        Session.deleteAll();
        ApiClient.deleteAll();
        AdminUser.deleteAll();
        JwtKey.deleteAll();
        Price.deleteAll();
        ModelYear.deleteAll();
        Model.deleteAll();
        Brand.deleteAll();
        VehicleType.deleteAll();
    }

    // ========== Session.isExpired() Tests ==========

    @Test
    @Transactional
    void testSession_IsExpired_WhenExpiredByTime() {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-1";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().minusHours(1); // Expirou há 1 hora
        session.isActive = true;
        session.persist();

        // Act
        boolean expired = session.isExpired();

        // Assert
        assertTrue(expired);
    }

    @Test
    @Transactional
    void testSession_IsExpired_WhenInactive() {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-2";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().plusHours(1); // Ainda não expirou
        session.isActive = false; // Mas está inativa
        session.persist();

        // Act
        boolean expired = session.isExpired();

        // Assert
        assertTrue(expired);
    }

    @Test
    @Transactional
    void testSession_IsNotExpired_WhenActiveAndNotExpired() {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-3";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().plusHours(1); // Ainda não expirou
        session.isActive = true; // E está ativa
        session.persist();

        // Act
        boolean expired = session.isExpired();

        // Assert
        assertFalse(expired);
    }

    @Test
    @Transactional
    void testSession_IsExpired_WhenInactiveAndExpired() {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-4";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().minusHours(1); // Expirou
        session.isActive = false; // E está inativa
        session.persist();

        // Act
        boolean expired = session.isExpired();

        // Assert
        assertTrue(expired);
    }

    // ========== @PrePersist Tests ==========

    @Test
    @Transactional
    void testSession_PrePersist_SetsCreatedAt() {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-5";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().plusHours(1);
        session.loginAt = null;
        session.lastActivityAt = null;

        LocalDateTime beforePersist = LocalDateTime.now();

        // Act
        session.persist();

        // Assert
        assertNotNull(session.createdAt);
        assertTrue(session.createdAt.isAfter(beforePersist.minusSeconds(1)) || 
                   session.createdAt.isEqual(beforePersist));
        assertTrue(session.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)) || 
                   session.createdAt.isEqual(LocalDateTime.now()));
    }

    @Test
    @Transactional
    void testSession_PrePersist_SetsLoginAtAndLastActivityAt_WhenNull() {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-6";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().plusHours(1);
        session.loginAt = null;
        session.lastActivityAt = null;

        // Act
        session.persist();

        // Assert
        assertNotNull(session.loginAt);
        assertNotNull(session.lastActivityAt);
        assertEquals(session.createdAt.truncatedTo(ChronoUnit.SECONDS), 
                     session.loginAt.truncatedTo(ChronoUnit.SECONDS));
        assertEquals(session.createdAt.truncatedTo(ChronoUnit.SECONDS), 
                     session.lastActivityAt.truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @Transactional
    void testSession_PrePersist_DoesNotOverrideLoginAt_WhenSet() {
        // Arrange
        ApiClient client = createTestClient();
        LocalDateTime customLoginAt = LocalDateTime.now().minusHours(2);
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-7";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().plusHours(1);
        session.loginAt = customLoginAt;
        session.lastActivityAt = null;

        // Act
        session.persist();

        // Assert
        assertEquals(customLoginAt.truncatedTo(ChronoUnit.SECONDS), 
                     session.loginAt.truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @Transactional
    void testAdminUser_PrePersist_SetsCreatedAt() {
        // Arrange
        AdminUser admin = new AdminUser();
        admin.name = "Test Admin";
        admin.username = "testadmin1";
        admin.email = "admin1@test.com";
        admin.password = "password";
        admin.isActive = true;

        LocalDateTime beforePersist = LocalDateTime.now();

        // Act
        admin.persist();

        // Assert
        assertNotNull(admin.createdAt);
        assertTrue(admin.createdAt.isAfter(beforePersist.minusSeconds(1)) || 
                   admin.createdAt.isEqual(beforePersist));
        assertTrue(admin.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)) || 
                   admin.createdAt.isEqual(LocalDateTime.now()));
    }

    @Test
    @Transactional
    void testApiClient_PrePersist_SetsCreatedAt() {
        // Arrange
        ApiClient client = new ApiClient();
        client.name = "Test Client";
        client.clientId = "test-client-1";
        client.email = "client1@test.com";
        client.clientSecret = "secret";
        client.isActive = true;

        LocalDateTime beforePersist = LocalDateTime.now();

        // Act
        client.persist();

        // Assert
        assertNotNull(client.createdAt);
        assertTrue(client.createdAt.isAfter(beforePersist.minusSeconds(1)) || 
                   client.createdAt.isEqual(beforePersist));
        assertTrue(client.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)) || 
                   client.createdAt.isEqual(LocalDateTime.now()));
    }

    @Test
    @Transactional
    void testVehicleType_PrePersist_SetsCreatedAt() {
        // Arrange
        VehicleType vehicleType = new VehicleType();
        vehicleType.name = "Carros";

        LocalDateTime beforePersist = LocalDateTime.now();

        // Act
        vehicleType.persist();

        // Assert
        assertNotNull(vehicleType.createdAt);
        assertTrue(vehicleType.createdAt.isAfter(beforePersist.minusSeconds(1)) || 
                   vehicleType.createdAt.isEqual(beforePersist));
        assertTrue(vehicleType.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)) || 
                   vehicleType.createdAt.isEqual(LocalDateTime.now()));
    }

    // ========== @PreUpdate Tests ==========

    @Test
    @Transactional
    void testSession_PreUpdate_SetsUpdatedAt() throws InterruptedException {
        // Arrange
        ApiClient client = createTestClient();
        Session session = new Session();
        session.apiClient = client;
        session.tokenJti = "test-jti-8";
        session.ipAddress = "127.0.0.1";
        session.userAgent = "TestAgent";
        session.expiresAt = LocalDateTime.now().plusHours(1);
        session.persist();

        LocalDateTime beforeUpdate = LocalDateTime.now();
        Thread.sleep(100); // Pequeno delay para garantir diferença de tempo

        // Act
        session.isActive = false;
        session.persist(); // Atualiza a entidade
        session.getEntityManager().flush(); // Força o flush para acionar @PreUpdate

        // Assert
        assertNotNull(session.updatedAt);
        assertTrue(session.updatedAt.isAfter(beforeUpdate) || 
                   session.updatedAt.isEqual(beforeUpdate));
    }

    @Test
    @Transactional
    void testAdminUser_PreUpdate_SetsUpdatedAt() throws InterruptedException {
        // Arrange
        AdminUser admin = new AdminUser();
        admin.name = "Test Admin";
        admin.username = "testadmin2";
        admin.email = "admin2@test.com";
        admin.password = "password";
        admin.isActive = true;
        admin.persist();

        LocalDateTime beforeUpdate = LocalDateTime.now();
        Thread.sleep(100);

        // Act
        admin.name = "Updated Admin";
        admin.persist();
        admin.getEntityManager().flush(); // Força o flush para acionar @PreUpdate

        // Assert
        assertNotNull(admin.updatedAt);
        assertTrue(admin.updatedAt.isAfter(beforeUpdate) || 
                   admin.updatedAt.isEqual(beforeUpdate));
    }

    @Test
    @Transactional
    void testApiClient_PreUpdate_SetsUpdatedAt() throws InterruptedException {
        // Arrange
        ApiClient client = new ApiClient();
        client.name = "Test Client";
        client.clientId = "test-client-2";
        client.email = "client2@test.com";
        client.clientSecret = "secret";
        client.isActive = true;
        client.persist();

        LocalDateTime beforeUpdate = LocalDateTime.now();
        Thread.sleep(100);

        // Act
        client.name = "Updated Client";
        client.persist();
        client.getEntityManager().flush(); // Força o flush para acionar @PreUpdate

        // Assert
        assertNotNull(client.updatedAt);
        assertTrue(client.updatedAt.isAfter(beforeUpdate) || 
                   client.updatedAt.isEqual(beforeUpdate));
    }

    @Test
    @Transactional
    void testVehicleType_PreUpdate_SetsUpdatedAt() throws InterruptedException {
        // Arrange
        VehicleType vehicleType = new VehicleType();
        vehicleType.name = "Carros";
        vehicleType.persist();

        LocalDateTime beforeUpdate = LocalDateTime.now();
        Thread.sleep(100);

        // Act
        vehicleType.name = "Automóveis";
        vehicleType.persist();
        vehicleType.getEntityManager().flush(); // Força o flush para acionar @PreUpdate

        // Assert
        assertNotNull(vehicleType.updatedAt);
        assertTrue(vehicleType.updatedAt.isAfter(beforeUpdate) || 
                   vehicleType.updatedAt.isEqual(beforeUpdate));
    }

    // ========== Unique Constraint Tests ==========

    @Test
    @Transactional
    void testAdminUser_UniqueConstraint_Username() {
        // Arrange
        AdminUser admin1 = new AdminUser();
        admin1.name = "Admin 1";
        admin1.username = "duplicate-username";
        admin1.email = "admin1@test.com";
        admin1.password = "password";
        admin1.isActive = true;
        admin1.persist();

        AdminUser admin2 = new AdminUser();
        admin2.name = "Admin 2";
        admin2.username = "duplicate-username"; // Mesmo username
        admin2.email = "admin2@test.com";
        admin2.password = "password";
        admin2.isActive = true;

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            admin2.persist();
            admin2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    @Test
    @Transactional
    void testApiClient_UniqueConstraint_ClientId() {
        // Arrange
        ApiClient client1 = new ApiClient();
        client1.name = "Client 1";
        client1.clientId = "duplicate-client-id";
        client1.email = "client1@test.com";
        client1.clientSecret = "secret";
        client1.isActive = true;
        client1.persist();

        ApiClient client2 = new ApiClient();
        client2.name = "Client 2";
        client2.clientId = "duplicate-client-id"; // Mesmo clientId
        client2.email = "client2@test.com";
        client2.clientSecret = "secret";
        client2.isActive = true;

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            client2.persist();
            client2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    @Test
    @Transactional
    void testApiClient_UniqueConstraint_Email() {
        // Arrange
        ApiClient client1 = new ApiClient();
        client1.name = "Client 1";
        client1.clientId = "client-1";
        client1.email = "duplicate@test.com";
        client1.clientSecret = "secret";
        client1.isActive = true;
        client1.persist();

        ApiClient client2 = new ApiClient();
        client2.name = "Client 2";
        client2.clientId = "client-2";
        client2.email = "duplicate@test.com"; // Mesmo email
        client2.clientSecret = "secret";
        client2.isActive = true;

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            client2.persist();
            client2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    @Test
    @Transactional
    void testVehicleType_UniqueConstraint_Name() {
        // Arrange
        VehicleType vehicleType1 = new VehicleType();
        vehicleType1.name = "Carros";
        vehicleType1.persist();

        VehicleType vehicleType2 = new VehicleType();
        vehicleType2.name = "Carros"; // Mesmo nome

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            vehicleType2.persist();
            vehicleType2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    @Test
    @Transactional
    void testJwtKey_UniqueConstraint_Kid() {
        // Arrange
        JwtKey key1 = new JwtKey();
        key1.kid = "duplicate-kid";
        key1.alg = com.exemplo.enums.JwtAlg.RS256;
        key1.publicKeyPem = "-----BEGIN PUBLIC KEY-----\ntest\n-----END PUBLIC KEY-----";
        key1.privateKeyCiphertext = "-----BEGIN PRIVATE KEY-----\ntest\n-----END PRIVATE KEY-----";
        key1.status = com.exemplo.enums.KeyStatus.ACTIVE;
        key1.persist();

        JwtKey key2 = new JwtKey();
        key2.kid = "duplicate-kid"; // Mesmo kid
        key2.alg = com.exemplo.enums.JwtAlg.RS256;
        key2.publicKeyPem = "-----BEGIN PUBLIC KEY-----\ntest2\n-----END PUBLIC KEY-----";
        key2.privateKeyCiphertext = "-----BEGIN PRIVATE KEY-----\ntest2\n-----END PRIVATE KEY-----";
        key2.status = com.exemplo.enums.KeyStatus.ACTIVE;

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            key2.persist();
            key2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    @Test
    @Transactional
    void testModel_UniqueConstraint_FipeCode() {
        // Arrange
        VehicleType vehicleType = new VehicleType();
        vehicleType.name = "Carros";
        vehicleType.persist();

        Brand brand = new Brand();
        brand.vehicleType = vehicleType;
        brand.externalCode = "21";
        brand.name = "Fiat";
        brand.persist();

        Model model1 = new Model();
        model1.brand = brand;
        model1.fipeCode = "001001";
        model1.name = "Uno";
        model1.persist();

        Model model2 = new Model();
        model2.brand = brand;
        model2.fipeCode = "001001"; // Mesmo fipeCode
        model2.name = "Palio";

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            model2.persist();
            model2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    @Test
    @Transactional
    void testModel_UniqueConstraint_BrandAndName() {
        // Arrange
        VehicleType vehicleType = new VehicleType();
        vehicleType.name = "Carros";
        vehicleType.persist();

        Brand brand = new Brand();
        brand.vehicleType = vehicleType;
        brand.externalCode = "21";
        brand.name = "Fiat";
        brand.persist();

        Model model1 = new Model();
        model1.brand = brand;
        model1.fipeCode = "001001";
        model1.name = "Uno";
        model1.persist();

        Model model2 = new Model();
        model2.brand = brand; // Mesma marca
        model2.fipeCode = "001002"; // FipeCode diferente
        model2.name = "Uno"; // Mesmo nome

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            model2.persist();
            model2.getEntityManager().flush(); // Força validação de constraints
        });
    }

    // ========== Helper Methods ==========

    private ApiClient createTestClient() {
        ApiClient client = new ApiClient();
        client.name = "Test Client";
        client.clientId = "test-client-" + System.currentTimeMillis();
        client.email = "test-client-" + System.currentTimeMillis() + "@test.com";
        client.clientSecret = "secret";
        client.isActive = true;
        client.persist();
        return client;
    }
}

