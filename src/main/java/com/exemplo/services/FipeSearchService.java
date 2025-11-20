package com.exemplo.services;

import com.exemplo.dto.FipeSearchDtos;
import com.exemplo.entities.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class FipeSearchService {

  private static final Logger LOG = Logger.getLogger(FipeSearchService.class);

  public FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> searchVehicleTypes(
      String name, int page, int pageSize) {
    
    String searchPattern = "%" + name.trim().toLowerCase() + "%";
    
    // Buscar tipos de veículo paginados
    PanacheQuery<VehicleType> query = VehicleType.find(
        "LOWER(name) LIKE ?1 AND deletedAt IS NULL ORDER BY name ASC",
        searchPattern
    );
    
    long totalItems = query.count();
    List<VehicleType> vehicleTypes = query.page(Page.of(page - 1, pageSize)).list();
    
    // Carregar dados relacionados de forma eficiente
    List<FipeSearchDtos.VehicleTypeResponse> vehicleTypeResponses = vehicleTypes.stream()
        .map(this::toVehicleTypeResponse)
        .collect(Collectors.toList());
    
    FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(page, pageSize, totalItems);
    
    return new FipeSearchDtos.PaginatedResponse<>(vehicleTypeResponses, meta);
  }

  private FipeSearchDtos.VehicleTypeResponse toVehicleTypeResponse(VehicleType vehicleType) {
    FipeSearchDtos.VehicleTypeResponse response = new FipeSearchDtos.VehicleTypeResponse();
    response.id = vehicleType.id;
    response.name = vehicleType.name;
    response.createdAt = vehicleType.createdAt;
    response.updatedAt = vehicleType.updatedAt;
    
    // Carregar marcas deste tipo de veículo
    List<Brand> brands = Brand.list(
        "vehicleType.id = ?1 AND deletedAt IS NULL ORDER BY name ASC",
        vehicleType.id
    );
    
    response.brands = brands.stream()
        .map(this::toBrandResponse)
        .collect(Collectors.toList());
    
    return response;
  }

  private FipeSearchDtos.BrandResponse toBrandResponse(Brand brand) {
    FipeSearchDtos.BrandResponse response = new FipeSearchDtos.BrandResponse();
    response.id = brand.id;
    response.name = brand.name;
    response.externalCode = brand.externalCode;
    response.createdAt = brand.createdAt;
    response.updatedAt = brand.updatedAt;
    
    // Carregar modelos desta marca
    List<Model> models = Model.list(
        "brand.id = ?1 AND deletedAt IS NULL ORDER BY name ASC",
        brand.id
    );
    
    response.models = models.stream()
        .map(this::toModelResponse)
        .collect(Collectors.toList());
    
    return response;
  }

  private FipeSearchDtos.ModelResponse toModelResponse(Model model) {
    FipeSearchDtos.ModelResponse response = new FipeSearchDtos.ModelResponse();
    response.id = model.id;
    response.name = model.name;
    response.model = model.model;
    response.version = model.version;
    response.fipeCode = model.fipeCode;
    response.createdAt = model.createdAt;
    response.updatedAt = model.updatedAt;
    
    // Carregar anos deste modelo
    List<ModelYear> modelYears = ModelYear.list(
        "model.id = ?1 AND deletedAt IS NULL ORDER BY yearModel DESC",
        model.id
    );
    
    response.years = modelYears.stream()
        .map(this::toModelYearResponse)
        .collect(Collectors.toList());
    
    return response;
  }

  private FipeSearchDtos.ModelYearResponse toModelYearResponse(ModelYear modelYear) {
    FipeSearchDtos.ModelYearResponse response = new FipeSearchDtos.ModelYearResponse();
    response.id = modelYear.id;
    response.yearModel = modelYear.yearModel;
    response.fuelCode = modelYear.fuelCode;
    response.fuelName = modelYear.fuelName;
    response.yearCode = modelYear.yearCode;
    response.fipeCode = modelYear.fipeCode;
    response.authentication = modelYear.authentication;
    response.createdAt = modelYear.createdAt;
    response.updatedAt = modelYear.updatedAt;
    
    // Carregar preços deste ano do modelo
    List<Price> prices = Price.list(
        "modelYear.id = ?1 AND deletedAt IS NULL ORDER BY referenceMonth DESC",
        modelYear.id
    );
    
    response.prices = prices.stream()
        .map(this::toPriceResponse)
        .collect(Collectors.toList());
    
    return response;
  }

  public FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> searchBrands(
      String name, String vehicleTypeName, int page, int pageSize) {
    
    String searchPattern = "%" + name.trim().toLowerCase() + "%";
    
    PanacheQuery<Brand> query;
    
    // Se vehicleTypeName foi fornecido, filtrar por tipo de veículo
    if (vehicleTypeName != null && !vehicleTypeName.trim().isEmpty()) {
      String vehicleTypePattern = "%" + vehicleTypeName.trim().toLowerCase() + "%";
      // Filtrar por nome da marca e nome do tipo de veículo
      query = Brand.find(
          "LOWER(name) LIKE ?1 AND LOWER(vehicleType.name) LIKE ?2 AND deletedAt IS NULL ORDER BY name ASC",
          searchPattern, vehicleTypePattern
      );
    } else {
      query = Brand.find(
          "LOWER(name) LIKE ?1 AND deletedAt IS NULL ORDER BY name ASC",
          searchPattern
      );
    }
    
    long totalItems = query.count();
    List<Brand> brands = query.page(Page.of(page - 1, pageSize)).list();
    
    // Carregar dados relacionados de forma eficiente
    List<FipeSearchDtos.BrandResponse> brandResponses = brands.stream()
        .map(this::toBrandResponse)
        .collect(Collectors.toList());
    
    FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(page, pageSize, totalItems);
    
    return new FipeSearchDtos.PaginatedResponse<>(brandResponses, meta);
  }

  private FipeSearchDtos.PriceResponse toPriceResponse(Price price) {
    FipeSearchDtos.PriceResponse response = new FipeSearchDtos.PriceResponse();
    response.id = price.id;
    response.referenceMonth = price.referenceMonth;
    response.value = price.value;
    response.currency = price.currency;
    response.authentication = price.authentication;
    response.consultedAt = price.consultedAt;
    response.createdAt = price.createdAt;
    response.updatedAt = price.updatedAt;
    return response;
  }
}

