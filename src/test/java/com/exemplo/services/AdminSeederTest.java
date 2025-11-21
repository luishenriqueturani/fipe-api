package com.exemplo.services;

import com.exemplo.entities.AdminUser;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AdminSeederTest {

    @Inject
    AdminSeeder adminSeeder;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste
        AdminUser.deleteAll();
    }

    @Test
    @Transactional
    void testOnStart_CreatesDefaultAdmin_WhenNotExists() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        adminSeeder.onStart(ev);

        // Assert
        AdminUser admin = AdminUser.find("email = ?1", "admin@fipe-api.local").firstResult();
        assertNotNull(admin);
        assertEquals("admin@fipe-api.local", admin.email);
        assertEquals("admin", admin.username);
        assertEquals("Administrador", admin.name);
        assertEquals("admin123", admin.password);
        assertTrue(admin.isActive);
        assertNotNull(admin.createdAt);
    }

    @Test
    @Transactional
    void testOnStart_DoesNotCreate_WhenAdminExistsByEmail() {
        // Arrange
        AdminUser existingAdmin = new AdminUser();
        existingAdmin.name = "Existing Admin";
        existingAdmin.username = "existing";
        existingAdmin.email = "admin@fipe-api.local";
        existingAdmin.password = "existing-password";
        existingAdmin.isActive = true;
        existingAdmin.persist();

        StartupEvent ev = new StartupEvent();

        // Act
        adminSeeder.onStart(ev);

        // Assert
        long count = AdminUser.count("email = ?1", "admin@fipe-api.local");
        assertEquals(1, count);
        
        AdminUser admin = AdminUser.find("email = ?1", "admin@fipe-api.local").firstResult();
        assertEquals("existing-password", admin.password); // Senha original mantida
        assertEquals("existing", admin.username); // Username original mantido
    }

    @Test
    @Transactional
    void testOnStart_DoesNotCreate_WhenAdminExistsByUsername() {
        // Arrange
        AdminUser existingAdmin = new AdminUser();
        existingAdmin.name = "Existing Admin";
        existingAdmin.username = "admin"; // Mesmo username
        existingAdmin.email = "different@test.com"; // Email diferente
        existingAdmin.password = "existing-password";
        existingAdmin.isActive = true;
        existingAdmin.persist();

        StartupEvent ev = new StartupEvent();

        // Act
        adminSeeder.onStart(ev);

        // Assert
        long count = AdminUser.count("username = ?1", "admin");
        assertEquals(1, count);
        
        AdminUser admin = AdminUser.find("username = ?1", "admin").firstResult();
        assertEquals("different@test.com", admin.email); // Email original mantido
        assertEquals("existing-password", admin.password); // Senha original mantida
    }

    @Test
    @Transactional
    void testOnStart_CreatesOnlyOneAdmin_WhenCalledMultipleTimes() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        adminSeeder.onStart(ev);
        adminSeeder.onStart(ev); // Chamar novamente

        // Assert
        long count = AdminUser.count("email = ?1", "admin@fipe-api.local");
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void testOnStart_SetsCorrectDefaultValues() {
        // Arrange
        StartupEvent ev = new StartupEvent();

        // Act
        adminSeeder.onStart(ev);

        // Assert
        AdminUser admin = AdminUser.find("email = ?1", "admin@fipe-api.local").firstResult();
        assertNotNull(admin);
        assertEquals("admin@fipe-api.local", admin.email);
        assertEquals("admin", admin.username);
        assertEquals("Administrador", admin.name);
        assertEquals("admin123", admin.password);
        assertTrue(admin.isActive);
        assertNull(admin.deletedAt);
        assertNotNull(admin.createdAt);
    }
}

