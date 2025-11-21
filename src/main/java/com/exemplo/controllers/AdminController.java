package com.exemplo.controllers;

import com.exemplo.dto.AuthDtos.AdminLoginRequest;
import com.exemplo.dto.AuthDtos.TokenResponse;
import com.exemplo.dto.SessionDtos;
import com.exemplo.entities.Session;
import com.exemplo.services.AuthService;
import com.exemplo.services.MetricsService;
import com.exemplo.services.SessionService;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.List;
import java.util.stream.Collectors;

import static com.exemplo.security.SecurityRoles.ADMIN;

@Path("/admin")
public class AdminController {
	@Inject
	AuthService authService;

	@Inject
	SessionService sessionService;

	@Inject
	MetricsService metricsService;

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Counted(
		name = "fipe_api_admin_login_requests_total",
		description = "Total de requisições de login de administradores",
		absolute = true
	)
	@Timed(
		name = "fipe_api_admin_login_duration",
		description = "Duração das requisições de login de administradores",
		unit = MetricUnits.MILLISECONDS,
		absolute = true
	)
	public Response login(AdminLoginRequest request, @Context HttpHeaders headers) {
		if (request == null || request.email == null || request.password == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Email e password são obrigatórios\"}")
					.build();
		}

		String ipAddress = headers.getHeaderString("X-Forwarded-For");
		if (ipAddress == null || ipAddress.isEmpty()) {
			ipAddress = headers.getHeaderString("X-Real-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty()) {
			ipAddress = "unknown";
		}
		String userAgent = headers.getHeaderString(HttpHeaders.USER_AGENT);
		if (userAgent == null) {
			userAgent = "unknown";
		}

		String jwt = authService.issueTokenForAdmin(request.email, request.password, ipAddress, userAgent);
		if (jwt == null) {
			metricsService.incrementFailedLogins();
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity("{\"error\": \"Credenciais inválidas\"}")
					.build();
		}

		// Incrementar métricas de sucesso
		metricsService.incrementSuccessfulLogins();
		metricsService.incrementAdminTokensIssued();

		// Token válido por 8 horas para admin
		return Response.ok(new TokenResponse(jwt, "bearer", 28800)).build();
	}

	@GET
	@Path("/sessions")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(ADMIN)
	public Response listActiveSessions() {
		List<Session> sessions = sessionService.getActiveSessions();
		List<SessionDtos.SessionResponse> response = sessions.stream()
				.map(this::toSessionResponse)
				.collect(Collectors.toList());
		return Response.ok(response).build();
	}

	@GET
	@Path("/sessions/client/{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(ADMIN)
	public Response listSessionsForClient(@PathParam("clientId") Long clientId) {
		List<Session> sessions = sessionService.getActiveSessionsForClient(clientId);
		List<SessionDtos.SessionResponse> response = sessions.stream()
				.map(this::toSessionResponse)
				.collect(Collectors.toList());
		return Response.ok(response).build();
	}

	@GET
	@Path("/sessions/admin/{adminId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(ADMIN)
	public Response listSessionsForAdmin(@PathParam("adminId") Long adminId) {
		List<Session> sessions = sessionService.getActiveSessionsForAdmin(adminId);
		List<SessionDtos.SessionResponse> response = sessions.stream()
				.map(this::toSessionResponse)
				.collect(Collectors.toList());
		return Response.ok(response).build();
	}

	@POST
	@Path("/sessions/logout")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(ADMIN)
	public Response logoutSession(@Context HttpHeaders headers) {
		String auth = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (auth == null || !auth.toLowerCase().startsWith("bearer ")) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity("{\"error\": \"Token de autenticação não fornecido\"}")
					.build();
		}

		String token = auth.substring(7);
		String tokenJti = null;
		try {
			var jwt = new DefaultJWTParser().parse(token);
			Object jti = jwt.getClaim("jti");
			if (jti != null) {
				tokenJti = jti.toString();
			}
		} catch (ParseException e) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity("{\"error\": \"Token inválido\"}")
					.build();
		}

		if (tokenJti == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Token não contém identificador de sessão\"}")
					.build();
		}

		sessionService.logoutSession(tokenJti);
		return Response.ok("{\"message\": \"Sessão encerrada com sucesso\"}").build();
	}

	private SessionDtos.SessionResponse toSessionResponse(Session session) {
		SessionDtos.SessionResponse response = new SessionDtos.SessionResponse();
		response.id = session.id;
		response.tokenJti = session.tokenJti;
		response.loginAt = session.loginAt;
		response.lastActivityAt = session.lastActivityAt;
		response.expiresAt = session.expiresAt;
		response.isActive = session.isActive;
		response.logoutAt = session.logoutAt;
		response.ipAddress = session.ipAddress;
		response.userAgent = session.userAgent;

		if (session.apiClient != null) {
			SessionDtos.ClientInfo clientInfo = new SessionDtos.ClientInfo();
			clientInfo.id = session.apiClient.id;
			clientInfo.name = session.apiClient.name;
			clientInfo.clientId = session.apiClient.clientId;
			response.client = clientInfo;
		}

		if (session.adminUser != null) {
			SessionDtos.AdminInfo adminInfo = new SessionDtos.AdminInfo();
			adminInfo.id = session.adminUser.id;
			adminInfo.name = session.adminUser.name;
			adminInfo.username = session.adminUser.username;
			adminInfo.email = session.adminUser.email;
			response.admin = adminInfo;
		}

		return response;
	}
}

