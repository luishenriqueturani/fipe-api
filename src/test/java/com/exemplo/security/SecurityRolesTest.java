package com.exemplo.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class SecurityRolesTest {

    @Test
    void testApiClientConstant_Value() {
        // Act & Assert
        assertEquals("api-client", SecurityRoles.API_CLIENT);
    }

    @Test
    void testAdminConstant_Value() {
        // Act & Assert
        assertEquals("admin", SecurityRoles.ADMIN);
    }

    @Test
    void testConstants_NotNull() {
        // Act & Assert
        assertNotNull(SecurityRoles.API_CLIENT);
        assertNotNull(SecurityRoles.ADMIN);
    }

    @Test
    void testConstants_NotEmpty() {
        // Act & Assert
        assertFalse(SecurityRoles.API_CLIENT.isEmpty());
        assertFalse(SecurityRoles.ADMIN.isEmpty());
    }

    @Test
    void testConstants_AreDifferent() {
        // Act & Assert
        assertNotEquals(SecurityRoles.API_CLIENT, SecurityRoles.ADMIN);
    }

    @Test
    void testClass_CannotBeInstantiated() throws Exception {
        // Arrange
        Constructor<SecurityRoles> constructor = SecurityRoles.class.getDeclaredConstructor();
        
        // Act & Assert - Verificar que o construtor é privado
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        
        // Verificar que o construtor existe e é privado
        // (não podemos instanciar a classe normalmente devido ao construtor privado)
        constructor.setAccessible(true);
        SecurityRoles instance = constructor.newInstance();
        assertNotNull(instance); // Pode ser instanciado via reflection, mas não normalmente
    }

    @Test
    void testClass_IsFinal() {
        // Act & Assert
        assertTrue(Modifier.isFinal(SecurityRoles.class.getModifiers()));
    }

    @Test
    void testConstants_ArePublicStaticFinal() throws Exception {
        // Arrange
        java.lang.reflect.Field apiClientField = SecurityRoles.class.getField("API_CLIENT");
        java.lang.reflect.Field adminField = SecurityRoles.class.getField("ADMIN");
        
        // Act & Assert
        assertTrue(Modifier.isPublic(apiClientField.getModifiers()));
        assertTrue(Modifier.isStatic(apiClientField.getModifiers()));
        assertTrue(Modifier.isFinal(apiClientField.getModifiers()));
        
        assertTrue(Modifier.isPublic(adminField.getModifiers()));
        assertTrue(Modifier.isStatic(adminField.getModifiers()));
        assertTrue(Modifier.isFinal(adminField.getModifiers()));
    }

    @Test
    void testConstants_AreStrings() {
        // Act & Assert
        assertInstanceOf(String.class, SecurityRoles.API_CLIENT);
        assertInstanceOf(String.class, SecurityRoles.ADMIN);
    }
}

