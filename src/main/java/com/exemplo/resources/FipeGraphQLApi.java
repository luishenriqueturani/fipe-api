package com.exemplo.resources;

import com.exemplo.dto.GraphQLDtos;
import com.exemplo.entities.Brand;
import com.exemplo.entities.Model;
import com.exemplo.entities.ModelYear;
import com.exemplo.entities.Price;
import com.exemplo.entities.VehicleType;
import com.exemplo.enums.Currency;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import java.util.ArrayList;
import java.util.List;

import static com.exemplo.security.SecurityRoles.API_CLIENT;

@GraphQLApi
@RolesAllowed(API_CLIENT)
public class FipeGraphQLApi {

	@Query
	@Description("Lista tipos de veículo")
	public List<VehicleType> vehicleTypes() {
		return VehicleType.list("deletedAt is null order by name asc");
	}

	@Query
	@Description("Lista marcas por tipo de veículo")
	public List<Brand> brandsByVehicleType(@Name("vehicleTypeId") @Id Long vehicleTypeId) {
		return Brand.list("vehicleType.id = ?1 and deletedAt is null order by name asc", vehicleTypeId);
	}

	@Query
	@Description("Lista modelos por marca")
	public List<Model> modelsByBrand(@Name("brandId") @Id Long brandId) {
		return Model.list("brand.id = ?1 and deletedAt is null order by name asc", brandId);
	}

	@Query
	@Description("Lista anos por modelo")
	public List<ModelYear> yearsByModel(@Name("modelId") @Id Long modelId) {
		return ModelYear.list("model.id = ?1 and deletedAt is null order by yearModel desc", modelId);
	}

	@Query
	@Description("Lista preços por ano de modelo (ordenado por mês referência desc)")
	public List<Price> pricesByModelYear(@Name("modelYearId") @Id Long modelYearId) {
		return Price.list("modelYear.id = ?1 and deletedAt is null order by referenceMonth desc", modelYearId);
	}

	// ========== BUSCAS FLEXÍVEIS COM FILTROS ==========

	@Query
	@Description("Busca flexível de tipos de veículo com filtros opcionais e paginação")
	public GraphQLDtos.PaginatedVehicleTypes searchVehicleTypes(
			@Name("filter") GraphQLDtos.VehicleTypeFilter filter,
			@Name("pagination") GraphQLDtos.PaginationInput pagination) {
		
		if (filter == null) {
			filter = new GraphQLDtos.VehicleTypeFilter();
		}
		if (pagination == null) {
			pagination = new GraphQLDtos.PaginationInput();
		}

		StringBuilder queryBuilder = new StringBuilder("deletedAt is null");
		List<Object> params = new ArrayList<>();
		int paramIndex = 1;

		if (filter.id != null) {
			queryBuilder.append(" AND id = ?").append(paramIndex++);
			params.add(filter.id);
		}

		if (filter.name != null && !filter.name.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.name.trim().toLowerCase() + "%");
		}

		queryBuilder.append(" ORDER BY name ASC");

		PanacheQuery<VehicleType> query = VehicleType.find(queryBuilder.toString(), params.toArray());
		long totalItems = query.count();
		List<VehicleType> vehicleTypes = query.page(Page.of(pagination.page - 1, pagination.pageSize)).list();

		GraphQLDtos.PaginationMeta meta = new GraphQLDtos.PaginationMeta(
				pagination.page, pagination.pageSize, totalItems);

		return new GraphQLDtos.PaginatedVehicleTypes(vehicleTypes, meta);
	}

	@Query
	@Description("Busca flexível de marcas com filtros opcionais e paginação")
	public GraphQLDtos.PaginatedBrands searchBrands(
			@Name("filter") GraphQLDtos.BrandFilter filter,
			@Name("pagination") GraphQLDtos.PaginationInput pagination) {
		
		if (filter == null) {
			filter = new GraphQLDtos.BrandFilter();
		}
		if (pagination == null) {
			pagination = new GraphQLDtos.PaginationInput();
		}

		StringBuilder queryBuilder = new StringBuilder("deletedAt is null");
		List<Object> params = new ArrayList<>();
		int paramIndex = 1;

		if (filter.id != null) {
			queryBuilder.append(" AND id = ?").append(paramIndex++);
			params.add(filter.id);
		}

		if (filter.name != null && !filter.name.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.name.trim().toLowerCase() + "%");
		}

		if (filter.vehicleTypeId != null) {
			queryBuilder.append(" AND vehicleType.id = ?").append(paramIndex++);
			params.add(filter.vehicleTypeId);
		}

		if (filter.vehicleTypeName != null && !filter.vehicleTypeName.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(vehicleType.name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.vehicleTypeName.trim().toLowerCase() + "%");
		}

		if (filter.externalCode != null && !filter.externalCode.trim().isEmpty()) {
			queryBuilder.append(" AND externalCode = ?").append(paramIndex++);
			params.add(filter.externalCode.trim());
		}

		queryBuilder.append(" ORDER BY name ASC");

		PanacheQuery<Brand> query = Brand.find(queryBuilder.toString(), params.toArray());
		long totalItems = query.count();
		List<Brand> brands = query.page(Page.of(pagination.page - 1, pagination.pageSize)).list();

		GraphQLDtos.PaginationMeta meta = new GraphQLDtos.PaginationMeta(
				pagination.page, pagination.pageSize, totalItems);

		return new GraphQLDtos.PaginatedBrands(brands, meta);
	}

	@Query
	@Description("Busca flexível de modelos com filtros opcionais e paginação")
	public GraphQLDtos.PaginatedModels searchModels(
			@Name("filter") GraphQLDtos.ModelFilter filter,
			@Name("pagination") GraphQLDtos.PaginationInput pagination) {
		
		if (filter == null) {
			filter = new GraphQLDtos.ModelFilter();
		}
		if (pagination == null) {
			pagination = new GraphQLDtos.PaginationInput();
		}

		StringBuilder queryBuilder = new StringBuilder("deletedAt is null");
		List<Object> params = new ArrayList<>();
		int paramIndex = 1;

		if (filter.id != null) {
			queryBuilder.append(" AND id = ?").append(paramIndex++);
			params.add(filter.id);
		}

		if (filter.name != null && !filter.name.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.name.trim().toLowerCase() + "%");
		}

		if (filter.brandId != null) {
			queryBuilder.append(" AND brand.id = ?").append(paramIndex++);
			params.add(filter.brandId);
		}

		if (filter.brandName != null && !filter.brandName.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(brand.name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.brandName.trim().toLowerCase() + "%");
		}

		if (filter.vehicleTypeId != null) {
			queryBuilder.append(" AND brand.vehicleType.id = ?").append(paramIndex++);
			params.add(filter.vehicleTypeId);
		}

		if (filter.vehicleTypeName != null && !filter.vehicleTypeName.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(brand.vehicleType.name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.vehicleTypeName.trim().toLowerCase() + "%");
		}

		if (filter.model != null && !filter.model.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(model) LIKE ?").append(paramIndex++);
			params.add("%" + filter.model.trim().toLowerCase() + "%");
		}

		if (filter.version != null && !filter.version.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(version) LIKE ?").append(paramIndex++);
			params.add("%" + filter.version.trim().toLowerCase() + "%");
		}

		if (filter.fipeCode != null && !filter.fipeCode.trim().isEmpty()) {
			queryBuilder.append(" AND fipeCode = ?").append(paramIndex++);
			params.add(filter.fipeCode.trim());
		}

		queryBuilder.append(" ORDER BY name ASC");

		PanacheQuery<Model> query = Model.find(queryBuilder.toString(), params.toArray());
		long totalItems = query.count();
		List<Model> models = query.page(Page.of(pagination.page - 1, pagination.pageSize)).list();

		GraphQLDtos.PaginationMeta meta = new GraphQLDtos.PaginationMeta(
				pagination.page, pagination.pageSize, totalItems);

		return new GraphQLDtos.PaginatedModels(models, meta);
	}

	@Query
	@Description("Busca flexível de anos de modelo com filtros opcionais e paginação")
	public GraphQLDtos.PaginatedModelYears searchModelYears(
			@Name("filter") GraphQLDtos.ModelYearFilter filter,
			@Name("pagination") GraphQLDtos.PaginationInput pagination) {
		
		if (filter == null) {
			filter = new GraphQLDtos.ModelYearFilter();
		}
		if (pagination == null) {
			pagination = new GraphQLDtos.PaginationInput();
		}

		StringBuilder queryBuilder = new StringBuilder("deletedAt is null");
		List<Object> params = new ArrayList<>();
		int paramIndex = 1;

		if (filter.id != null) {
			queryBuilder.append(" AND id = ?").append(paramIndex++);
			params.add(filter.id);
		}

		if (filter.modelId != null) {
			queryBuilder.append(" AND model.id = ?").append(paramIndex++);
			params.add(filter.modelId);
		}

		if (filter.modelName != null && !filter.modelName.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(model.name) LIKE ?").append(paramIndex++);
			params.add("%" + filter.modelName.trim().toLowerCase() + "%");
		}

		if (filter.yearModel != null) {
			queryBuilder.append(" AND yearModel = ?").append(paramIndex++);
			params.add(filter.yearModel);
		}

		if (filter.fuelCode != null && !filter.fuelCode.trim().isEmpty()) {
			queryBuilder.append(" AND fuelCode = ?").append(paramIndex++);
			params.add(filter.fuelCode.trim());
		}

		if (filter.fuelName != null && !filter.fuelName.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(fuelName) LIKE ?").append(paramIndex++);
			params.add("%" + filter.fuelName.trim().toLowerCase() + "%");
		}

		if (filter.yearCode != null && !filter.yearCode.trim().isEmpty()) {
			queryBuilder.append(" AND yearCode = ?").append(paramIndex++);
			params.add(filter.yearCode.trim());
		}

		if (filter.fipeCode != null && !filter.fipeCode.trim().isEmpty()) {
			queryBuilder.append(" AND fipeCode = ?").append(paramIndex++);
			params.add(filter.fipeCode.trim());
		}

		queryBuilder.append(" ORDER BY yearModel DESC");

		PanacheQuery<ModelYear> query = ModelYear.find(queryBuilder.toString(), params.toArray());
		long totalItems = query.count();
		List<ModelYear> modelYears = query.page(Page.of(pagination.page - 1, pagination.pageSize)).list();

		GraphQLDtos.PaginationMeta meta = new GraphQLDtos.PaginationMeta(
				pagination.page, pagination.pageSize, totalItems);

		return new GraphQLDtos.PaginatedModelYears(modelYears, meta);
	}

	@Query
	@Description("Busca flexível de preços com filtros opcionais e paginação")
	public GraphQLDtos.PaginatedPrices searchPrices(
			@Name("filter") GraphQLDtos.PriceFilter filter,
			@Name("pagination") GraphQLDtos.PaginationInput pagination) {
		
		if (filter == null) {
			filter = new GraphQLDtos.PriceFilter();
		}
		if (pagination == null) {
			pagination = new GraphQLDtos.PaginationInput();
		}

		StringBuilder queryBuilder = new StringBuilder("deletedAt is null");
		List<Object> params = new ArrayList<>();
		int paramIndex = 1;

		if (filter.id != null) {
			queryBuilder.append(" AND id = ?").append(paramIndex++);
			params.add(filter.id);
		}

		if (filter.modelYearId != null) {
			queryBuilder.append(" AND modelYear.id = ?").append(paramIndex++);
			params.add(filter.modelYearId);
		}

		if (filter.referenceMonth != null && !filter.referenceMonth.trim().isEmpty()) {
			queryBuilder.append(" AND LOWER(referenceMonth) LIKE ?").append(paramIndex++);
			params.add("%" + filter.referenceMonth.trim().toLowerCase() + "%");
		}

		if (filter.currency != null && !filter.currency.trim().isEmpty()) {
			try {
				Currency currencyEnum = Currency.valueOf(filter.currency.toUpperCase());
				queryBuilder.append(" AND currency = ?").append(paramIndex++);
				params.add(currencyEnum);
			} catch (IllegalArgumentException e) {
				// Ignora se o valor não for um enum válido
			}
		}

		queryBuilder.append(" ORDER BY referenceMonth DESC");

		PanacheQuery<Price> query = Price.find(queryBuilder.toString(), params.toArray());
		long totalItems = query.count();
		List<Price> prices = query.page(Page.of(pagination.page - 1, pagination.pageSize)).list();

		GraphQLDtos.PaginationMeta meta = new GraphQLDtos.PaginationMeta(
				pagination.page, pagination.pageSize, totalItems);

		return new GraphQLDtos.PaginatedPrices(prices, meta);
	}
}


