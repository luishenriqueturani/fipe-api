package com.exemplo.controllers;

import com.exemplo.dto.FipeDataDtos.FipeDataRequest;
import com.exemplo.services.FipeDataService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import static com.exemplo.security.SecurityRoles.ADMIN;

@Path("/fipe-data")
public class FipeDataController {

  private static final Logger LOG = Logger.getLogger(FipeDataController.class);

  @Inject
  FipeDataService fipeDataService;

  @POST
  @Path("/update")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN)
  public Response updateFipeData(FipeDataRequest request) {
    try {
      LOG.info("Recebida requisição para atualizar dados da FIPE");

      if (request == null) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"error\": \"Dados da FIPE não fornecidos\"}")
            .build();
      }

      // Validar se pelo menos um tipo de veículo foi fornecido
      if ((request.cars == null || request.cars.isEmpty()) &&
          (request.trucks == null || request.trucks.isEmpty()) &&
          (request.motorCycles == null || request.motorCycles.isEmpty())) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"error\": \"Pelo menos um tipo de veículo deve ser fornecido\"}")
            .build();
      }

      fipeDataService.processFipeData(request);

      LOG.info("Dados da FIPE atualizados com sucesso");
      return Response.ok("{\"message\": \"Dados da FIPE atualizados com sucesso\"}")
          .build();

    } catch (Exception e) {
      LOG.error("Erro ao processar dados da FIPE", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"error\": \"Erro interno do servidor: " + e.getMessage() + "\"}")
          .build();
    }
  }
}
