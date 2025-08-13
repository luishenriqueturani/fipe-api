package com.exemplo.filters;

import com.exemplo.entities.ApiAccessLog;
import com.exemplo.entities.ApiClient;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;

@Provider
@RequestScoped
public class AccessLogFilter implements ContainerRequestFilter, ContainerResponseFilter {
	@Context
	UriInfo uriInfo;

	private long startNs;
	private String clientId;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		startNs = System.nanoTime();
		String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
			String token = auth.substring(7);
			try {
				var jwt = new DefaultJWTParser().parse(token);
				Object cid = jwt.getClaim("client_id");
				if (cid != null) {
					clientId = cid.toString();
				}
			} catch (ParseException ignored) {}
		}
	}

	@Override
	@Transactional
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		ApiAccessLog log = new ApiAccessLog();
		ApiClient client = null;
		if (clientId != null) {
			client = ApiClient.find("clientId", clientId).firstResult();
		}
		log.apiClient = client;
		log.method = requestContext.getMethod();
		log.path = uriInfo.getPath();
		log.query = uriInfo.getRequestUri().getQuery();
		log.statusCode = responseContext.getStatus();
		log.ip = requestContext.getHeaders().getFirst("X-Forwarded-For");
		log.userAgent = requestContext.getHeaderString("User-Agent");
		log.durationMs = (System.nanoTime() - startNs) / 1_000_000L;
		log.createdAt = LocalDateTime.now();
		log.persist();
	}
}
