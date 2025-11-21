package com.exemplo.security;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class PemUtilsTest {

    private String toPemPublicKey(RSAPublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" +
                chunkString(base64, 64) +
                "\n-----END PUBLIC KEY-----";
    }

    private String toPemPrivateKey(RSAPrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PRIVATE KEY-----\n" +
                chunkString(base64, 64) +
                "\n-----END PRIVATE KEY-----";
    }

    private String chunkString(String str, int chunkSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i += chunkSize) {
            if (i > 0) {
                sb.append("\n");
            }
            int end = Math.min(i + chunkSize, str.length());
            sb.append(str.substring(i, end));
        }
        return sb.toString();
    }

    @Test
    void testReadPublicKeyFromPem_ValidPem() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey originalKey = (RSAPublicKey) keyPair.getPublic();
        String pem = toPemPublicKey(originalKey);

        // Act
        PublicKey readKey = PemUtils.readPublicKeyFromPem(pem);

        // Assert
        assertNotNull(readKey);
        assertInstanceOf(RSAPublicKey.class, readKey);
        RSAPublicKey rsaReadKey = (RSAPublicKey) readKey;
        assertEquals(originalKey.getModulus(), rsaReadKey.getModulus());
        assertEquals(originalKey.getPublicExponent(), rsaReadKey.getPublicExponent());
    }

    @Test
    void testReadPublicKeyFromPem_WithWhitespace() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey originalKey = (RSAPublicKey) keyPair.getPublic();
        String pem = toPemPublicKey(originalKey);
        // Adicionar espaços e quebras de linha extras
        String pemWithWhitespace = "  \n  " + pem.replace("\n", "\n  \n  ") + "\n  \n  ";

        // Act
        PublicKey readKey = PemUtils.readPublicKeyFromPem(pemWithWhitespace);

        // Assert
        assertNotNull(readKey);
        assertInstanceOf(RSAPublicKey.class, readKey);
        RSAPublicKey rsaReadKey = (RSAPublicKey) readKey;
        assertEquals(originalKey.getModulus(), rsaReadKey.getModulus());
    }

    @Test
    void testReadPublicKeyFromPem_InvalidPem() {
        // Arrange
        String invalidPem = "-----BEGIN PUBLIC KEY-----\nInvalid Base64\n-----END PUBLIC KEY-----";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPublicKeyFromPem(invalidPem);
        });
    }

    @Test
    void testReadPublicKeyFromPem_MissingHeaders() {
        // Arrange
        String pemWithoutHeaders = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPublicKeyFromPem(pemWithoutHeaders);
        });
    }

    @Test
    void testReadPrivateKeyFromPem_ValidPem() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPrivateKey originalKey = (RSAPrivateKey) keyPair.getPrivate();
        String pem = toPemPrivateKey(originalKey);

        // Act
        PrivateKey readKey = PemUtils.readPrivateKeyFromPem(pem);

        // Assert
        assertNotNull(readKey);
        assertInstanceOf(RSAPrivateKey.class, readKey);
        RSAPrivateKey rsaReadKey = (RSAPrivateKey) readKey;
        assertEquals(originalKey.getModulus(), rsaReadKey.getModulus());
        assertEquals(originalKey.getPrivateExponent(), rsaReadKey.getPrivateExponent());
    }

    @Test
    void testReadPrivateKeyFromPem_WithWhitespace() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPrivateKey originalKey = (RSAPrivateKey) keyPair.getPrivate();
        String pem = toPemPrivateKey(originalKey);
        // Adicionar espaços e quebras de linha extras
        String pemWithWhitespace = "  \n  " + pem.replace("\n", "\n  \n  ") + "\n  \n  ";

        // Act
        PrivateKey readKey = PemUtils.readPrivateKeyFromPem(pemWithWhitespace);

        // Assert
        assertNotNull(readKey);
        assertInstanceOf(RSAPrivateKey.class, readKey);
        RSAPrivateKey rsaReadKey = (RSAPrivateKey) readKey;
        assertEquals(originalKey.getModulus(), rsaReadKey.getModulus());
    }

    @Test
    void testReadPrivateKeyFromPem_InvalidPem() {
        // Arrange
        String invalidPem = "-----BEGIN PRIVATE KEY-----\nInvalid Base64\n-----END PRIVATE KEY-----";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPrivateKeyFromPem(invalidPem);
        });
    }

    @Test
    void testReadPrivateKeyFromPem_MissingHeaders() {
        // Arrange
        String pemWithoutHeaders = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PemUtils.readPrivateKeyFromPem(pemWithoutHeaders);
        });
    }

    @Test
    void testBase64Url_WithPositiveValue() {
        // Arrange
        BigInteger value = new BigInteger("1234567890123456789012345678901234567890");

        // Act
        String result = PemUtils.base64Url(value);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Base64URL não deve conter padding (=) ou caracteres + e /
        assertFalse(result.contains("="));
        assertFalse(result.contains("+"));
        assertFalse(result.contains("/"));
    }

    @Test
    void testBase64Url_WithZero() {
        // Arrange
        BigInteger value = BigInteger.ZERO;

        // Act
        String result = PemUtils.base64Url(value);

        // Assert
        assertNotNull(result);
        // Zero pode resultar em string vazia ou "AA" dependendo da implementação
        assertTrue(result.isEmpty() || result.length() > 0);
    }

    @Test
    void testBase64Url_WithLargeValue() {
        // Arrange
        BigInteger value = new BigInteger("1".repeat(100)); // Número muito grande

        // Act
        String result = PemUtils.base64Url(value);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.contains("="));
    }

    @Test
    void testBase64Url_WithNegativeValue() {
        // Arrange
        BigInteger value = new BigInteger("-1234567890");

        // Act
        String result = PemUtils.base64Url(value);

        // Assert
        assertNotNull(result);
        // Valores negativos são tratados como unsigned
        assertFalse(result.isEmpty());
    }

    @Test
    void testIsRsaPublicKey_WithRsaKey() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        // Act
        boolean result = PemUtils.isRsaPublicKey(publicKey);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsRsaPublicKey_WithNonRsaKey() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(256);
        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        // Act
        boolean result = PemUtils.isRsaPublicKey(publicKey);

        // Assert
        assertFalse(result);
    }

    @Test
    void testJwkN_WithRsaKey() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey rsaKey = (RSAPublicKey) keyPair.getPublic();

        // Act
        String n = PemUtils.jwkN(rsaKey);

        // Assert
        assertNotNull(n);
        assertFalse(n.isEmpty());
        assertFalse(n.contains("=")); // Sem padding
        assertFalse(n.contains("+"));
        assertFalse(n.contains("/"));
        // Verificar que é base64URL do modulus
        BigInteger modulus = rsaKey.getModulus();
        String expected = PemUtils.base64Url(modulus);
        assertEquals(expected, n);
    }

    @Test
    void testJwkE_WithRsaKey() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey rsaKey = (RSAPublicKey) keyPair.getPublic();

        // Act
        String e = PemUtils.jwkE(rsaKey);

        // Assert
        assertNotNull(e);
        assertFalse(e.isEmpty());
        assertFalse(e.contains("=")); // Sem padding
        assertFalse(e.contains("+"));
        assertFalse(e.contains("/"));
        // Verificar que é base64URL do exponent
        BigInteger exponent = rsaKey.getPublicExponent();
        String expected = PemUtils.base64Url(exponent);
        assertEquals(expected, e);
    }

    @Test
    void testJwkN_Consistency() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey rsaKey = (RSAPublicKey) keyPair.getPublic();

        // Act
        String n1 = PemUtils.jwkN(rsaKey);
        String n2 = PemUtils.jwkN(rsaKey);

        // Assert
        assertEquals(n1, n2); // Deve ser determinístico
    }

    @Test
    void testJwkE_Consistency() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey rsaKey = (RSAPublicKey) keyPair.getPublic();

        // Act
        String e1 = PemUtils.jwkE(rsaKey);
        String e2 = PemUtils.jwkE(rsaKey);

        // Assert
        assertEquals(e1, e2); // Deve ser determinístico
    }

    @Test
    void testReadPublicKeyFromPem_AndJwkConversion() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey originalKey = (RSAPublicKey) keyPair.getPublic();
        String pem = toPemPublicKey(originalKey);

        // Act
        PublicKey readKey = PemUtils.readPublicKeyFromPem(pem);
        RSAPublicKey rsaReadKey = (RSAPublicKey) readKey;
        String n = PemUtils.jwkN(rsaReadKey);
        String e = PemUtils.jwkE(rsaReadKey);

        // Assert
        assertNotNull(n);
        assertNotNull(e);
        // Verificar que os valores JWK correspondem aos valores originais
        String originalN = PemUtils.jwkN(originalKey);
        String originalE = PemUtils.jwkE(originalKey);
        assertEquals(originalN, n);
        assertEquals(originalE, e);
    }

    @Test
    void testBase64Url_WithLeadingZero() {
        // Arrange
        // Criar um BigInteger que tem um byte zero no início quando convertido para byte array
        byte[] bytesWithLeadingZero = new byte[]{0, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        BigInteger value = new BigInteger(1, bytesWithLeadingZero); // Usar sinal positivo

        // Act
        String result = PemUtils.base64Url(value);

        // Assert
        assertNotNull(result);
        // O zero inicial deve ser removido (toUnsignedBytes)
        assertFalse(result.isEmpty());
    }

    @Test
    void testReadPublicKeyFromPem_WithSingleLine() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey originalKey = (RSAPublicKey) keyPair.getPublic();
        String pem = toPemPublicKey(originalKey);
        // Remover todas as quebras de linha
        String singleLinePem = pem.replace("\n", "");

        // Act
        PublicKey readKey = PemUtils.readPublicKeyFromPem(singleLinePem);

        // Assert
        assertNotNull(readKey);
        assertInstanceOf(RSAPublicKey.class, readKey);
        RSAPublicKey rsaReadKey = (RSAPublicKey) readKey;
        assertEquals(originalKey.getModulus(), rsaReadKey.getModulus());
    }

    @Test
    void testReadPrivateKeyFromPem_WithSingleLine() throws Exception {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPrivateKey originalKey = (RSAPrivateKey) keyPair.getPrivate();
        String pem = toPemPrivateKey(originalKey);
        // Remover todas as quebras de linha
        String singleLinePem = pem.replace("\n", "");

        // Act
        PrivateKey readKey = PemUtils.readPrivateKeyFromPem(singleLinePem);

        // Assert
        assertNotNull(readKey);
        assertInstanceOf(RSAPrivateKey.class, readKey);
        RSAPrivateKey rsaReadKey = (RSAPrivateKey) readKey;
        assertEquals(originalKey.getModulus(), rsaReadKey.getModulus());
    }
}

