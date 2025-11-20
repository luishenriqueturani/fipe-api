package com.exemplo.controllers;

import com.exemplo.services.JwtKeyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/.well-known")
public class JwksController {

	@Inject
	JwtKeyService jwtKeyService;

	@GET
	@Path("/jwks.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJwks() {
		var keys = jwtKeyService.buildJwks();
		var jwks = Map.of("keys", keys);
		return Response.ok(jwks).build();
	}
}

