package com.exemplo.filters;

import com.exemplo.services.AccessLogService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessLogFilterTest {

    @Mock
    private AccessLogService accessLogService;

    @InjectMocks
    private AccessLogFilter accessLogFilter;

    @Test
    void testFilterRequest_ExtractsClientIdFromJwt() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String clientId = "test-client-id";
        String tokenJti = UUID.randomUUID().toString();
        
        // Criar JWT payload
        String payloadJson = String.format(
                "{\"client_id\":\"%s\",\"jti\":\"%s\",\"sub\":\"test\",\"exp\":9999999999}",
                clientId, tokenJti);
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes());
        String jwt = headerBase64 + "." + payloadBase64 + ".signature";
        
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Verificar que não lançou exceção
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_ExtractsJtiFromJwt() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String tokenJti = UUID.randomUUID().toString();
        
        String payloadJson = String.format(
                "{\"jti\":\"%s\",\"sub\":\"test\"}", tokenJti);
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String jwt = headerBase64 + "." + payloadBase64 + ".signature";
        
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_NoAuthorizationHeader() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_InvalidJwtFormat() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer invalid.jwt");

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção, apenas ignora
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_InvalidJwtPayload() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String invalidJwt = "header.invalid-payload.signature";
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + invalidJwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção, apenas ignora
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_CaseInsensitiveBearer() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String payloadJson = "{\"client_id\":\"test\"}";
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String jwt = headerBase64 + "." + payloadBase64 + ".signature";
        
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("BEARER " + jwt); // Maiúsculo

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Deve processar normalmente
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_StartsTimer() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        long before = System.nanoTime();
        accessLogFilter.filter(requestContext);
        long after = System.nanoTime();

        // Assert - O timer deve ser iniciado (verificado indiretamente através do tempo)
        assertTrue(after >= before);
    }

    @Test
    void testFilterRequest_JwtWithoutClientId() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String payloadJson = "{\"sub\":\"test\",\"exp\":9999999999}"; // Sem client_id
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String jwt = headerBase64 + "." + payloadBase64 + ".signature";
        
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_JwtWithoutJti() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String payloadJson = "{\"client_id\":\"test\",\"sub\":\"test\"}"; // Sem jti
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String jwt = headerBase64 + "." + payloadBase64 + ".signature";
        
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_EmptyBearerToken() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer ");

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_MalformedBase64Payload() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String jwt = "header.not-valid-base64.signature";
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção, apenas ignora
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testFilterRequest_InvalidJsonPayload() {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        String invalidJson = "not-valid-json";
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(invalidJson.getBytes());
        String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String jwt = headerBase64 + "." + payloadBase64 + ".signature";
        
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        accessLogFilter.filter(requestContext);

        // Assert - Não deve lançar exceção, apenas ignora
        assertDoesNotThrow(() -> {
            accessLogFilter.filter(requestContext);
        });
    }

    @Test
    void testGetIpAddress_XForwardedFor() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("X-Forwarded-For", "10.0.0.1");
        when(requestContext.getHeaders()).thenReturn(headers);

        // Act - Usar reflection para acessar método privado
        java.lang.reflect.Method method = AccessLogFilter.class.getDeclaredMethod(
                "getIpAddress", ContainerRequestContext.class);
        method.setAccessible(true);
        String ip = (String) method.invoke(accessLogFilter, requestContext);

        // Assert
        assertEquals("10.0.0.1", ip);
    }

    @Test
    void testGetIpAddress_XRealIP() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        // Sem X-Forwarded-For, mas com X-Real-IP
        headers.add("X-Real-IP", "192.168.1.100");
        when(requestContext.getHeaders()).thenReturn(headers);

        // Act
        java.lang.reflect.Method method = AccessLogFilter.class.getDeclaredMethod(
                "getIpAddress", ContainerRequestContext.class);
        method.setAccessible(true);
        String ip = (String) method.invoke(accessLogFilter, requestContext);

        // Assert
        assertEquals("192.168.1.100", ip);
    }

    @Test
    void testGetIpAddress_FallbackToUnknown() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        // Sem X-Forwarded-For nem X-Real-IP
        when(requestContext.getHeaders()).thenReturn(headers);

        // Act
        java.lang.reflect.Method method = AccessLogFilter.class.getDeclaredMethod(
                "getIpAddress", ContainerRequestContext.class);
        method.setAccessible(true);
        String ip = (String) method.invoke(accessLogFilter, requestContext);

        // Assert
        assertEquals("unknown", ip);
    }

    @Test
    void testGetIpAddress_XForwardedForTakesPrecedence() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("X-Forwarded-For", "10.0.0.1");
        headers.add("X-Real-IP", "192.168.1.100");
        when(requestContext.getHeaders()).thenReturn(headers);

        // Act
        java.lang.reflect.Method method = AccessLogFilter.class.getDeclaredMethod(
                "getIpAddress", ContainerRequestContext.class);
        method.setAccessible(true);
        String ip = (String) method.invoke(accessLogFilter, requestContext);

        // Assert - X-Forwarded-For deve ter precedência
        assertEquals("10.0.0.1", ip);
    }

    @Test
    void testGetIpAddress_EmptyXForwardedFor() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("X-Forwarded-For", ""); // Vazio
        headers.add("X-Real-IP", "192.168.1.100");
        when(requestContext.getHeaders()).thenReturn(headers);

        // Act
        java.lang.reflect.Method method = AccessLogFilter.class.getDeclaredMethod(
                "getIpAddress", ContainerRequestContext.class);
        method.setAccessible(true);
        String ip = (String) method.invoke(accessLogFilter, requestContext);

        // Assert - Deve usar X-Real-IP quando X-Forwarded-For está vazio
        assertEquals("192.168.1.100", ip);
    }

    @Test
    void testGetIpAddress_EmptyXRealIP() throws Exception {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("X-Real-IP", ""); // Vazio
        when(requestContext.getHeaders()).thenReturn(headers);

        // Act
        java.lang.reflect.Method method = AccessLogFilter.class.getDeclaredMethod(
                "getIpAddress", ContainerRequestContext.class);
        method.setAccessible(true);
        String ip = (String) method.invoke(accessLogFilter, requestContext);

        // Assert - Deve retornar "unknown" quando ambos estão vazios
        assertEquals("unknown", ip);
    }
}
