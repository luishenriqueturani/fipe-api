package com.exemplo.filters;

import com.exemplo.services.AccessLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.util.Base64;

@Provider
@RequestScoped
public class AccessLogFilter implements ContainerRequestFilter, ContainerResponseFilter {
	@Context
	UriInfo uriInfo;

	@Inject
	AccessLogService accessLogService;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private long startNs;
	private String clientId;
	private String tokenJti;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		startNs = System.nanoTime();
		String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
			String token = auth.substring(7);
			try {
				// Parsing básico do JWT para extrair claims sem validação
				// JWT tem formato: header.payload.signature
				String[] parts = token.split("\\.");
				if (parts.length == 3) {
					// Decodificar o payload (parte 2)
					String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
					JsonNode claims = objectMapper.readTree(payload);
					
					// Extrair client_id
					if (claims.has("client_id")) {
						clientId = claims.get("client_id").asText();
					}
					
					// Extrair jti
					if (claims.has("jti")) {
						tokenJti = claims.get("jti").asText();
					}
				}
			} catch (Exception ignored) {
				// Se falhar o parsing, simplesmente ignora
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		// Executar operações de banco de forma assíncrona para não bloquear o thread de I/O
		// Capturar valores finais antes de executar assincronamente
		final String finalTokenJti = tokenJti;
		final String finalClientId = clientId;
		final String method = requestContext.getMethod();
		final String path = uriInfo.getPath();
		final String query = uriInfo.getRequestUri().getQuery();
		final int statusCode = responseContext.getStatus();
		final String ip = getIpAddress(requestContext);
		final String userAgent = requestContext.getHeaderString("User-Agent");
		final long durationMs = (System.nanoTime() - startNs) / 1_000_000L;

		// Executar de forma assíncrona em um worker thread
		Uni.createFrom().item(() -> {
			accessLogService.logAccess(finalTokenJti, finalClientId, method, path, 
					query, statusCode, ip, userAgent, durationMs);
			return null;
		})
		.runSubscriptionOn(java.util.concurrent.ForkJoinPool.commonPool())
		.subscribe().with(
			result -> {},
			failure -> {
				// Log do erro mas não interrompe a resposta
				System.err.println("Erro ao registrar log de acesso: " + failure.getMessage());
			}
		);
	}

	private String getIpAddress(ContainerRequestContext requestContext) {
		String ip = requestContext.getHeaders().getFirst("X-Forwarded-For");
		if (ip == null || ip.isEmpty()) {
			ip = requestContext.getHeaders().getFirst("X-Real-IP");
		}
		if (ip == null || ip.isEmpty()) {
			ip = "unknown";
		}
		return ip;
	}
}
