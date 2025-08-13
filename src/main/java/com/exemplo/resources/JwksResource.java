package com.exemplo.resources;

import com.exemplo.services.JwtKeyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.Map;

@Path("/.well-known")
public class JwksResource {
	@Inject
	JwtKeyService keyService;

	@GET
	@Path("/jwks.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> jwks() {
		Map<String, Object> jwks = new HashMap<>();
		jwks.put("keys", keyService.buildJwks());
		return jwks;
	}
}
