package com.exemplo.controllers;

import com.exemplo.dto.AuthDtos.TokenRequest;
import com.exemplo.dto.AuthDtos.TokenResponse;
import com.exemplo.services.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/oauth/token")
public class AuthController {
	@Inject
	AuthService authService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response token(TokenRequest request) {
		String jwt = authService.issueTokenForClient(request.clientId, request.clientSecret);
		if (jwt == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		// exp fixo de 10 min conforme serviço
		return Response.ok(new TokenResponse(jwt, "bearer", 600)).build();
	}
}
