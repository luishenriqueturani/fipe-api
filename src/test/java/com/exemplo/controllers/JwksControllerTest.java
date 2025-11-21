package com.exemplo.controllers;

import com.exemplo.services.JwtKeyService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwksControllerTest {

    @Mock
    private JwtKeyService jwtKeyService;

    @InjectMocks
    private JwksController jwksController;

    @Test
    void testGetJwks_Success() {
        // Arrange
        List<Map<String, Object>> mockKeys = new ArrayList<>();
        Map<String, Object> key1 = Map.of(
                "kty", "RSA",
                "alg", "RS256",
                "use", "sig",
                "kid", "key-1",
                "n", "modulus-value",
                "e", "AQAB"
        );
        mockKeys.add(key1);

        when(jwtKeyService.buildJwks()).thenReturn(mockKeys);

        // Act
        Response response = jwksController.getJwks();

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jwks = (Map<String, Object>) response.getEntity();
        assertTrue(jwks.containsKey("keys"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        assertEquals(1, keys.size());
        assertEquals("key-1", keys.get(0).get("kid"));
        
        verify(jwtKeyService, times(1)).buildJwks();
    }

    @Test
    void testGetJwks_EmptyKeys() {
        // Arrange
        List<Map<String, Object>> emptyKeys = new ArrayList<>();
        when(jwtKeyService.buildJwks()).thenReturn(emptyKeys);

        // Act
        Response response = jwksController.getJwks();

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jwks = (Map<String, Object>) response.getEntity();
        assertTrue(jwks.containsKey("keys"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        assertTrue(keys.isEmpty());
        
        verify(jwtKeyService, times(1)).buildJwks();
    }

    @Test
    void testGetJwks_MultipleKeys() {
        // Arrange
        List<Map<String, Object>> mockKeys = new ArrayList<>();
        Map<String, Object> key1 = Map.of(
                "kty", "RSA",
                "alg", "RS256",
                "use", "sig",
                "kid", "key-1",
                "n", "modulus-value-1",
                "e", "AQAB"
        );
        Map<String, Object> key2 = Map.of(
                "kty", "RSA",
                "alg", "RS256",
                "use", "sig",
                "kid", "key-2",
                "n", "modulus-value-2",
                "e", "AQAB"
        );
        mockKeys.add(key1);
        mockKeys.add(key2);

        when(jwtKeyService.buildJwks()).thenReturn(mockKeys);

        // Act
        Response response = jwksController.getJwks();

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jwks = (Map<String, Object>) response.getEntity();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        assertEquals(2, keys.size());
        
        verify(jwtKeyService, times(1)).buildJwks();
    }
}

