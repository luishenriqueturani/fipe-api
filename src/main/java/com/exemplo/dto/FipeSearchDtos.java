package com.exemplo.dto;

import com.exemplo.enums.Currency;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FipeSearchDtos {

  public static class PaginatedResponse<T> {
    public List<T> data;
    public PaginationMeta meta;

    public PaginatedResponse(List<T> data, PaginationMeta meta) {
      this.data = data;
      this.meta = meta;
    }
  }

  public static class PaginationMeta {
    public int page;
    public int pageSize;
    public long totalItems;
    public int totalPages;
    public boolean hasNext;
    public boolean hasPrevious;

    public PaginationMeta(int page, int pageSize, long totalItems) {
      this.page = page;
      this.pageSize = pageSize;
      this.totalItems = totalItems;
      this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
      this.hasNext = page < totalPages;
      this.hasPrevious = page > 1;
    }
  }

  public static class VehicleTypeResponse {
    public Long id;
    public String name;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<BrandResponse> brands;
  }

  public static class BrandResponse {
    public Long id;
    public String name;
    public String externalCode;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<ModelResponse> models;
  }

  public static class ModelResponse {
    public Long id;
    public String name;
    public String model;
    public String version;
    public String fipeCode;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<ModelYearResponse> years;
  }

  public static class ModelYearResponse {
    public Long id;
    public Integer yearModel;
    public String fuelCode;
    public String fuelName;
    public String yearCode;
    public String fipeCode;
    public String authentication;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<PriceResponse> prices;
  }

  public static class PriceResponse {
    public Long id;
    public String referenceMonth;
    public BigDecimal value;
    public Currency currency;
    public String authentication;
    public LocalDateTime consultedAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
  }
}

