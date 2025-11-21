package com.exemplo.services;

import com.exemplo.dto.FipeSearchDtos;
import com.exemplo.entities.*;
import com.exemplo.enums.Currency;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class FipeSearchServiceTest {

    @Inject
    FipeSearchService fipeSearchService;

    private VehicleType carros;
    private VehicleType caminhoes;
    private Brand fiat;
    private Brand volvo;
    private Model uno;
    private Model fh540;
    private ModelYear uno2022;
    private ModelYear fh5402023;
    private Price priceUno;
    private Price priceFh540;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste anteriores
        Price.deleteAll();
        ModelYear.deleteAll();
        Model.deleteAll();
        Brand.deleteAll();
        VehicleType.deleteAll();

        // Criar tipos de veículo
        carros = new VehicleType();
        carros.name = "Carros";
        carros.persist();

        caminhoes = new VehicleType();
        caminhoes.name = "Caminhões";
        caminhoes.persist();

        // Criar marcas
        fiat = new Brand();
        fiat.vehicleType = carros;
        fiat.externalCode = "1";
        fiat.name = "Fiat";
        fiat.persist();

        volvo = new Brand();
        volvo.vehicleType = caminhoes;
        volvo.externalCode = "2";
        volvo.name = "Volvo";
        volvo.persist();

        // Criar modelos
        uno = new Model();
        uno.brand = fiat;
        uno.name = "Uno 1.0";
        uno.model = "Uno";
        uno.version = "1.0";
        uno.fipeCode = "038003";
        uno.persist();

        fh540 = new Model();
        fh540.brand = volvo;
        fh540.name = "FH 540";
        fh540.model = "FH";
        fh540.version = "540";
        fh540.fipeCode = "123456";
        fh540.persist();

        // Criar anos de modelo
        uno2022 = new ModelYear();
        uno2022.model = uno;
        uno2022.yearModel = 2022;
        uno2022.fuelCode = "GAS";
        uno2022.fuelName = "Gasolina";
        uno2022.yearCode = "2022 Gasolina";
        uno2022.fipeCode = "038003-2";
        uno2022.authentication = "abc123";
        uno2022.persist();

        fh5402023 = new ModelYear();
        fh5402023.model = fh540;
        fh5402023.yearModel = 2023;
        fh5402023.fuelCode = "DIE";
        fh5402023.fuelName = "Diesel";
        fh5402023.yearCode = "2023 Diesel";
        fh5402023.fipeCode = "123456-1";
        fh5402023.authentication = "def456";
        fh5402023.persist();

        // Criar preços
        priceUno = new Price();
        priceUno.modelYear = uno2022;
        priceUno.referenceMonth = "setembro de 2025";
        priceUno.value = new BigDecimal("45000.00");
        priceUno.currency = Currency.BRL;
        priceUno.authentication = "abc123";
        priceUno.consultedAt = LocalDateTime.now();
        priceUno.persist();

        priceFh540 = new Price();
        priceFh540.modelYear = fh5402023;
        priceFh540.referenceMonth = "setembro de 2025";
        priceFh540.value = new BigDecimal("350000.00");
        priceFh540.currency = Currency.BRL;
        priceFh540.authentication = "def456";
        priceFh540.consultedAt = LocalDateTime.now();
        priceFh540.persist();
    }

    @Test
    @Transactional
    void testSearchVehicleTypes_ByName() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> response = 
                fipeSearchService.searchVehicleTypes("Carros", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Carros", response.data.get(0).name);
        assertNotNull(response.meta);
        assertEquals(1, response.meta.page);
        assertEquals(10, response.meta.pageSize);
        assertEquals(1, response.meta.totalItems);
        assertEquals(1, response.meta.totalPages);
        assertFalse(response.meta.hasNext);
        assertFalse(response.meta.hasPrevious);
    }

    @Test
    @Transactional
    void testSearchVehicleTypes_PartialMatch() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> response = 
                fipeSearchService.searchVehicleTypes("Car", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Carros", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchVehicleTypes_CaseInsensitive() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> response = 
                fipeSearchService.searchVehicleTypes("caminhões", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Caminhões", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchVehicleTypes_WithPagination() {
        // Arrange - Criar mais tipos de veículo
        VehicleType motos = new VehicleType();
        motos.name = "Motocicletas";
        motos.persist();

        // Act - Primeira página
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> page1 = 
                fipeSearchService.searchVehicleTypes("", 1, 2);

        // Assert
        assertNotNull(page1);
        assertEquals(2, page1.data.size());
        assertEquals(1, page1.meta.page);
        assertEquals(2, page1.meta.pageSize);
        assertEquals(3, page1.meta.totalItems);
        assertEquals(2, page1.meta.totalPages);
        assertTrue(page1.meta.hasNext);
        assertFalse(page1.meta.hasPrevious);

        // Act - Segunda página
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> page2 = 
                fipeSearchService.searchVehicleTypes("", 2, 2);

        // Assert
        assertNotNull(page2);
        assertEquals(1, page2.data.size());
        assertEquals(2, page2.meta.page);
        assertFalse(page2.meta.hasNext);
        assertTrue(page2.meta.hasPrevious);
    }

    @Test
    @Transactional
    void testSearchVehicleTypes_IncludesBrandsAndModels() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> response = 
                fipeSearchService.searchVehicleTypes("Carros", 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        FipeSearchDtos.VehicleTypeResponse vehicleType = response.data.get(0);
        assertNotNull(vehicleType.brands);
        assertEquals(1, vehicleType.brands.size());
        assertEquals("Fiat", vehicleType.brands.get(0).name);
        assertNotNull(vehicleType.brands.get(0).models);
        assertEquals(1, vehicleType.brands.get(0).models.size());
        assertEquals("Uno 1.0", vehicleType.brands.get(0).models.get(0).name);
    }

    @Test
    @Transactional
    void testSearchVehicleTypes_EmptyResult() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> response = 
                fipeSearchService.searchVehicleTypes("Não Existe", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertTrue(response.data.isEmpty());
        assertEquals(0, response.meta.totalItems);
    }

    @Test
    @Transactional
    void testSearchBrands_ByName() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> response = 
                fipeSearchService.searchBrands("Fiat", null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Fiat", response.data.get(0).name);
        assertEquals("1", response.data.get(0).externalCode);
    }

    @Test
    @Transactional
    void testSearchBrands_ByVehicleType() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> response = 
                fipeSearchService.searchBrands("", "Carros", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Fiat", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchBrands_ByNameAndVehicleType() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> response = 
                fipeSearchService.searchBrands("Volvo", "Caminhões", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Volvo", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchBrands_WithPagination() {
        // Arrange - Criar mais marcas
        Brand honda = new Brand();
        honda.vehicleType = carros;
        honda.externalCode = "3";
        honda.name = "Honda";
        honda.persist();

        // Act - Primeira página
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> page1 = 
                fipeSearchService.searchBrands("", null, 1, 2);

        // Assert
        assertNotNull(page1);
        assertEquals(2, page1.data.size());
        assertEquals(1, page1.meta.page);
        assertEquals(2, page1.meta.pageSize);
        assertEquals(3, page1.meta.totalItems);
        assertTrue(page1.meta.hasNext);
    }

    @Test
    @Transactional
    void testSearchBrands_IncludesModels() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> response = 
                fipeSearchService.searchBrands("Fiat", null, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        FipeSearchDtos.BrandResponse brand = response.data.get(0);
        assertNotNull(brand.models);
        assertEquals(1, brand.models.size());
        assertEquals("Uno 1.0", brand.models.get(0).name);
    }

    @Test
    @Transactional
    void testSearchBrands_CaseInsensitive() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> response = 
                fipeSearchService.searchBrands("fiat", null, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        assertEquals("Fiat", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_ByName() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Uno", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Uno 1.0", response.data.get(0).name);
        assertEquals("Uno", response.data.get(0).model);
        assertEquals("1.0", response.data.get(0).version);
    }

    @Test
    @Transactional
    void testSearchModels_ByBrandName() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("", "Fiat", null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Uno 1.0", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_ByVehicleTypeName() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("", null, "Caminhões", null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("FH 540", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_ByModelBase() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("", null, null, "Uno", null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Uno 1.0", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_ByVersion() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("", null, null, null, "1.0", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Uno 1.0", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_WithMultipleFilters() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Uno", "Fiat", "Carros", "Uno", "1.0", 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertEquals(1, response.data.size());
        assertEquals("Uno 1.0", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_WithPagination() {
        // Arrange - Criar mais modelos
        Model palio = new Model();
        palio.brand = fiat;
        palio.name = "Palio 1.4";
        palio.model = "Palio";
        palio.version = "1.4";
        palio.fipeCode = "038004";
        palio.persist();

        // Act - Primeira página
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> page1 = 
                fipeSearchService.searchModels("", "Fiat", null, null, null, 1, 1);

        // Assert
        assertNotNull(page1);
        assertEquals(1, page1.data.size());
        assertEquals(1, page1.meta.page);
        assertEquals(1, page1.meta.pageSize);
        assertEquals(2, page1.meta.totalItems);
        assertEquals(2, page1.meta.totalPages);
        assertTrue(page1.meta.hasNext);
    }

    @Test
    @Transactional
    void testSearchModels_IncludesYearsAndPrices() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Uno", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        FipeSearchDtos.ModelResponse model = response.data.get(0);
        assertNotNull(model.years);
        assertEquals(1, model.years.size());
        assertEquals(2022, model.years.get(0).yearModel);
        assertNotNull(model.years.get(0).prices);
        assertEquals(1, model.years.get(0).prices.size());
        assertEquals(new BigDecimal("45000.00"), model.years.get(0).prices.get(0).value);
    }

    @Test
    @Transactional
    void testSearchModels_CaseInsensitive() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("uno", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        assertEquals("Uno 1.0", response.data.get(0).name);
    }

    @Test
    @Transactional
    void testSearchModels_EmptyResult() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Não Existe", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertTrue(response.data.isEmpty());
        assertEquals(0, response.meta.totalItems);
    }

    @Test
    @Transactional
    void testSearchModels_FilterByNonExistentBrand() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("", "Não Existe", null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertNotNull(response.data);
        assertTrue(response.data.isEmpty());
    }

    @Test
    @Transactional
    void testToVehicleTypeResponse_CompleteData() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> response = 
                fipeSearchService.searchVehicleTypes("Carros", 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        FipeSearchDtos.VehicleTypeResponse vehicleType = response.data.get(0);
        assertEquals(carros.id, vehicleType.id);
        assertEquals("Carros", vehicleType.name);
        assertNotNull(vehicleType.createdAt);
        assertNotNull(vehicleType.brands);
    }

    @Test
    @Transactional
    void testToBrandResponse_CompleteData() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> response = 
                fipeSearchService.searchBrands("Fiat", null, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        FipeSearchDtos.BrandResponse brand = response.data.get(0);
        assertEquals(fiat.id, brand.id);
        assertEquals("Fiat", brand.name);
        assertEquals("1", brand.externalCode);
        assertNotNull(brand.createdAt);
        assertNotNull(brand.models);
    }

    @Test
    @Transactional
    void testToModelResponse_CompleteData() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Uno", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.data.size());
        FipeSearchDtos.ModelResponse model = response.data.get(0);
        assertEquals(uno.id, model.id);
        assertEquals("Uno 1.0", model.name);
        assertEquals("Uno", model.model);
        assertEquals("1.0", model.version);
        assertEquals("038003", model.fipeCode);
        assertNotNull(model.createdAt);
        assertNotNull(model.years);
    }

    @Test
    @Transactional
    void testToModelYearResponse_CompleteData() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Uno", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        FipeSearchDtos.ModelYearResponse modelYear = response.data.get(0).years.get(0);
        assertEquals(uno2022.id, modelYear.id);
        assertEquals(2022, modelYear.yearModel);
        assertEquals("GAS", modelYear.fuelCode);
        assertEquals("Gasolina", modelYear.fuelName);
        assertEquals("2022 Gasolina", modelYear.yearCode);
        assertEquals("038003-2", modelYear.fipeCode);
        assertEquals("abc123", modelYear.authentication);
        assertNotNull(modelYear.createdAt);
        assertNotNull(modelYear.prices);
    }

    @Test
    @Transactional
    void testToPriceResponse_CompleteData() {
        // Act
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> response = 
                fipeSearchService.searchModels("Uno", null, null, null, null, 1, 10);

        // Assert
        assertNotNull(response);
        FipeSearchDtos.PriceResponse price = response.data.get(0).years.get(0).prices.get(0);
        assertEquals(priceUno.id, price.id);
        assertEquals("setembro de 2025", price.referenceMonth);
        assertEquals(new BigDecimal("45000.00"), price.value);
        assertEquals(Currency.BRL, price.currency);
        assertEquals("abc123", price.authentication);
        assertNotNull(price.consultedAt);
        assertNotNull(price.createdAt);
    }
}

