package com.exemplo.resources;

import com.exemplo.entities.Brand;
import com.exemplo.entities.Model;
import com.exemplo.entities.ModelYear;
import com.exemplo.entities.Price;
import com.exemplo.entities.VehicleType;
import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

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
}


