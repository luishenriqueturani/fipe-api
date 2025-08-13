package com.exemplo.security;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {
	private PemUtils() {}

	public static PrivateKey readPrivateKeyFromPem(String pem) {
		String sanitized = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		byte[] der = Base64.getDecoder().decode(sanitized);
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(der);
			return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
		} catch (Exception e) {
			throw new IllegalArgumentException("Falha ao ler PRIVATE KEY (PKCS#8)", e);
		}
	}

	public static PublicKey readPublicKeyFromPem(String pem) {
		String sanitized = pem
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");
		byte[] der = Base64.getDecoder().decode(sanitized);
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(der);
			return KeyFactory.getInstance("RSA").generatePublic(keySpec);
		} catch (Exception e) {
			throw new IllegalArgumentException("Falha ao ler PUBLIC KEY", e);
		}
	}

	public static String base64Url(BigInteger value) {
		byte[] bytes = toUnsignedBytes(value);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static byte[] toUnsignedBytes(BigInteger value) {
		byte[] bytes = value.toByteArray();
		if (bytes.length > 1 && bytes[0] == 0) {
			byte[] trimmed = new byte[bytes.length - 1];
			System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
			return trimmed;
		}
		return bytes;
	}

	public static boolean isRsaPublicKey(PublicKey key) {
		return key instanceof RSAPublicKey;
	}

	public static String jwkN(RSAPublicKey rsa) {
		return base64Url(rsa.getModulus());
	}

	public static String jwkE(RSAPublicKey rsa) {
		return base64Url(rsa.getPublicExponent());
	}
}
