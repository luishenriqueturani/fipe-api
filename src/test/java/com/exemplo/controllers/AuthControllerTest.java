package com.exemplo.controllers;

import com.exemplo.dto.AuthDtos;
import com.exemplo.services.AuthService;
import com.exemplo.services.MetricsService;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Request request;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private AuthController authController;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = mock(HttpHeaders.class);
    }

    @Test
    void testToken_Success() {
        // Arrange
        AuthDtos.TokenRequest tokenRequest = new AuthDtos.TokenRequest();
        tokenRequest.email = "client@test.com";
        tokenRequest.password = "password123";

        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...";
        
        when(httpHeaders.getHeaderString("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(httpHeaders.getHeaderString(HttpHeaders.USER_AGENT)).thenReturn("Mozilla/5.0");
        when(authService.issueTokenForClient(eq("client@test.com"), eq("password123"), 
                eq("192.168.1.1"), eq("Mozilla/5.0"))).thenReturn(jwtToken);

        // Act
        Response response = authController.token(tokenRequest, httpHeaders);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        AuthDtos.TokenResponse tokenResponse = (AuthDtos.TokenResponse) response.getEntity();
        assertEquals(jwtToken, tokenResponse.access_token);
        assertEquals("bearer", tokenResponse.token_type);
        assertEquals(600L, tokenResponse.expires_in);
        
        verify(authService, times(1)).issueTokenForClient(
                eq("client@test.com"), eq("password123"), eq("192.168.1.1"), eq("Mozilla/5.0"));
    }

    @Test
    void testToken_InvalidCredentials() {
        // Arrange
        AuthDtos.TokenRequest tokenRequest = new AuthDtos.TokenRequest();
        tokenRequest.email = "client@test.com";
        tokenRequest.password = "wrongpassword";

        when(httpHeaders.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(httpHeaders.getHeaderString("X-Real-IP")).thenReturn("192.168.1.1");
        when(httpHeaders.getHeaderString(HttpHeaders.USER_AGENT)).thenReturn("Mozilla/5.0");
        when(authService.issueTokenForClient(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        // Act
        Response response = authController.token(tokenRequest, httpHeaders);

        // Assert
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        
        verify(authService, times(1)).issueTokenForClient(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testToken_UnknownIpAddress() {
        // Arrange
        AuthDtos.TokenRequest tokenRequest = new AuthDtos.TokenRequest();
        tokenRequest.email = "client@test.com";
        tokenRequest.password = "password123";

        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...";
        
        when(httpHeaders.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(httpHeaders.getHeaderString("X-Real-IP")).thenReturn(null);
        when(httpHeaders.getHeaderString(HttpHeaders.USER_AGENT)).thenReturn(null);
        when(authService.issueTokenForClient(eq("client@test.com"), eq("password123"), 
                eq("unknown"), eq("unknown"))).thenReturn(jwtToken);

        // Act
        Response response = authController.token(tokenRequest, httpHeaders);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(authService, times(1)).issueTokenForClient(
                eq("client@test.com"), eq("password123"), eq("unknown"), eq("unknown"));
    }

    @Test
    void testToken_XRealIPUsed() {
        // Arrange
        AuthDtos.TokenRequest tokenRequest = new AuthDtos.TokenRequest();
        tokenRequest.email = "client@test.com";
        tokenRequest.password = "password123";

        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...";
        
        when(httpHeaders.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(httpHeaders.getHeaderString("X-Real-IP")).thenReturn("10.0.0.1");
        when(httpHeaders.getHeaderString(HttpHeaders.USER_AGENT)).thenReturn("Mozilla/5.0");
        when(authService.issueTokenForClient(eq("client@test.com"), eq("password123"), 
                eq("10.0.0.1"), eq("Mozilla/5.0"))).thenReturn(jwtToken);

        // Act
        Response response = authController.token(tokenRequest, httpHeaders);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(authService, times(1)).issueTokenForClient(
                eq("client@test.com"), eq("password123"), eq("10.0.0.1"), eq("Mozilla/5.0"));
    }
}

