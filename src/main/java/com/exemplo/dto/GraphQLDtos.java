package com.exemplo.dto;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Type;

/**
 * DTOs para uso em GraphQL
 */
public class GraphQLDtos {

	@Input("VehicleTypeFilter")
	@Type("VehicleTypeFilter")
	public static class VehicleTypeFilter {
		public String name;
		public Long id;
	}

	@Input("BrandFilter")
	@Type("BrandFilter")
	public static class BrandFilter {
		public String name;
		public Long id;
		public Long vehicleTypeId;
		public String vehicleTypeName;
		public String externalCode;
	}

	@Input("ModelFilter")
	@Type("ModelFilter")
	public static class ModelFilter {
		public String name;
		public Long id;
		public Long brandId;
		public String brandName;
		public Long vehicleTypeId;
		public String vehicleTypeName;
		public String model;
		public String version;
		public String fipeCode;
	}

	@Input("ModelYearFilter")
	@Type("ModelYearFilter")
	public static class ModelYearFilter {
		public Long id;
		public Long modelId;
		public String modelName;
		public Integer yearModel;
		public String fuelCode;
		public String fuelName;
		public String yearCode;
		public String fipeCode;
	}

	@Input("PriceFilter")
	@Type("PriceFilter")
	public static class PriceFilter {
		public Long id;
		public Long modelYearId;
		public String referenceMonth;
		public String currency;
	}

	@Input("PaginationInput")
	@Type("PaginationInput")
	public static class PaginationInput {
		public int page = 1;
		public int pageSize = 20;
	}

	@Type("PaginationMeta")
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

	@Type("PaginatedVehicleTypes")
	public static class PaginatedVehicleTypes {
		public java.util.List<com.exemplo.entities.VehicleType> data;
		public PaginationMeta meta;

		public PaginatedVehicleTypes(java.util.List<com.exemplo.entities.VehicleType> data, PaginationMeta meta) {
			this.data = data;
			this.meta = meta;
		}
	}

	@Type("PaginatedBrands")
	public static class PaginatedBrands {
		public java.util.List<com.exemplo.entities.Brand> data;
		public PaginationMeta meta;

		public PaginatedBrands(java.util.List<com.exemplo.entities.Brand> data, PaginationMeta meta) {
			this.data = data;
			this.meta = meta;
		}
	}

	@Type("PaginatedModels")
	public static class PaginatedModels {
		public java.util.List<com.exemplo.entities.Model> data;
		public PaginationMeta meta;

		public PaginatedModels(java.util.List<com.exemplo.entities.Model> data, PaginationMeta meta) {
			this.data = data;
			this.meta = meta;
		}
	}

	@Type("PaginatedModelYears")
	public static class PaginatedModelYears {
		public java.util.List<com.exemplo.entities.ModelYear> data;
		public PaginationMeta meta;

		public PaginatedModelYears(java.util.List<com.exemplo.entities.ModelYear> data, PaginationMeta meta) {
			this.data = data;
			this.meta = meta;
		}
	}

	@Type("PaginatedPrices")
	public static class PaginatedPrices {
		public java.util.List<com.exemplo.entities.Price> data;
		public PaginationMeta meta;

		public PaginatedPrices(java.util.List<com.exemplo.entities.Price> data, PaginationMeta meta) {
			this.data = data;
			this.meta = meta;
		}
	}
}

