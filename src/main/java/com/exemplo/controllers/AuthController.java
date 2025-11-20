package com.exemplo.controllers;

import com.exemplo.dto.AuthDtos.TokenRequest;
import com.exemplo.dto.AuthDtos.TokenResponse;
import com.exemplo.services.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

@Path("/oauth/token")
public class AuthController {
	@Inject
	AuthService authService;

	@Context
	Request request;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response token(TokenRequest tokenRequest, @Context jakarta.ws.rs.core.HttpHeaders headers) {
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

		String jwt = authService.issueTokenForClient(tokenRequest.email, tokenRequest.password, ipAddress, userAgent);
		if (jwt == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		// exp fixo de 10 min conforme serviço
		return Response.ok(new TokenResponse(jwt, "bearer", 600)).build();
	}
}
