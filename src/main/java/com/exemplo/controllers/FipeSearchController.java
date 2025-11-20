package com.exemplo.controllers;

import com.exemplo.dto.FipeSearchDtos;
import com.exemplo.services.FipeSearchService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import static com.exemplo.security.SecurityRoles.ADMIN;
import static com.exemplo.security.SecurityRoles.API_CLIENT;

@Path("/api")
public class FipeSearchController {

  private static final Logger LOG = Logger.getLogger(FipeSearchController.class);

  @Inject
  FipeSearchService fipeSearchService;

  @GET
  @Path("/vehicle-types/search")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({API_CLIENT, ADMIN})
  public Response searchVehicleTypes(
      @QueryParam("name") String name,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
    try {
      LOG.info("Buscando tipos de veículo com nome: " + name + " (page: " + page + ", pageSize: " + pageSize + ")");

      if (name == null || name.trim().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"error\": \"Parâmetro 'name' é obrigatório\"}")
            .build();
      }

      if (page < 1) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"error\": \"Parâmetro 'page' deve ser maior ou igual a 1\"}")
            .build();
      }

      if (pageSize < 1 || pageSize > 100) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"error\": \"Parâmetro 'pageSize' deve estar entre 1 e 100\"}")
            .build();
      }

      FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> result = 
          fipeSearchService.searchVehicleTypes(name, page, pageSize);

      LOG.info("Encontrados " + result.meta.totalItems + " tipos de veículo (página " + page + " de " + result.meta.totalPages + ")");
      return Response.ok(result).build();

    } catch (Exception e) {
      LOG.error("Erro ao buscar tipos de veículo", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"error\": \"Erro interno do servidor: " + e.getMessage() + "\"}")
          .build();
    }
  }
}

