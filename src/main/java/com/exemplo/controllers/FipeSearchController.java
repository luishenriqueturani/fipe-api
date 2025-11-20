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

  @GET
  @Path("/brands/search")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({API_CLIENT, ADMIN})
  public Response searchBrands(
      @QueryParam("name") String name,
      @QueryParam("vehicleType") String vehicleType,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
    try {
      LOG.info("Buscando marcas com nome: " + name + 
          (vehicleType != null ? " (tipo de veículo: " + vehicleType + ")" : "") + 
          " (page: " + page + ", pageSize: " + pageSize + ")");

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

      FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> result = 
          fipeSearchService.searchBrands(name, vehicleType, page, pageSize);

      LOG.info("Encontradas " + result.meta.totalItems + " marcas (página " + page + " de " + result.meta.totalPages + ")");
      return Response.ok(result).build();

    } catch (Exception e) {
      LOG.error("Erro ao buscar marcas", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"error\": \"Erro interno do servidor: " + e.getMessage() + "\"}")
          .build();
    }
  }

  @GET
  @Path("/models/search")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({API_CLIENT, ADMIN})
  public Response searchModels(
      @QueryParam("name") String name,
      @QueryParam("brand") String brand,
      @QueryParam("vehicleType") String vehicleType,
      @QueryParam("model") String modelBase,
      @QueryParam("version") String version,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
    try {
      LOG.info("Buscando modelos com nome: " + name + 
          (brand != null ? " (marca: " + brand + ")" : "") +
          (vehicleType != null ? " (tipo: " + vehicleType + ")" : "") +
          (modelBase != null ? " (modelo base: " + modelBase + ")" : "") +
          (version != null ? " (versão: " + version + ")" : "") +
          " (page: " + page + ", pageSize: " + pageSize + ")");

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

      FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> result = 
          fipeSearchService.searchModels(name, brand, vehicleType, modelBase, version, page, pageSize);

      LOG.info("Encontrados " + result.meta.totalItems + " modelos (página " + page + " de " + result.meta.totalPages + ")");
      return Response.ok(result).build();

    } catch (Exception e) {
      LOG.error("Erro ao buscar modelos", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"error\": \"Erro interno do servidor: " + e.getMessage() + "\"}")
          .build();
    }
  }
}

