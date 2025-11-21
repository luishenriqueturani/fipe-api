package com.exemplo.services;

import com.exemplo.dto.FipeDataDtos;
import com.exemplo.entities.*;
import com.exemplo.enums.Currency;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class FipeDataServiceTest {

    @Inject
    FipeDataService fipeDataService;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de teste anteriores
        Price.deleteAll();
        ModelYear.deleteAll();
        Model.deleteAll();
        Brand.deleteAll();
        VehicleType.deleteAll();
    }

    @Test
    @Transactional
    void testProcessFipeData_WithCars() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno 1.0";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "038003-2";
        year.modelYear = "2022 Gasolina";
        year.authentication = "abc123";
        year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("45000.00");
        averagePrice.formattedValue = "R$ 45.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        car.models.add(model);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        assertNotNull(vehicleType);
        assertEquals("Carros", vehicleType.name);

        Brand brand = Brand.find("vehicleType.id = ?1 and externalCode = ?2", 
                vehicleType.id, "1").firstResult();
        assertNotNull(brand);
        assertEquals("Fiat", brand.name);
        assertEquals("1", brand.externalCode);

        Model modelEntity = Model.find("brand.id = ?1 and name = ?2", 
                brand.id, "Uno 1.0").firstResult();
        assertNotNull(modelEntity);
        assertEquals("Uno 1.0", modelEntity.name);
        assertEquals("038003", modelEntity.fipeCode); // Base fipeCode sem sufixo

        ModelYear modelYear = ModelYear.find("model.id = ?1", modelEntity.id).firstResult();
        assertNotNull(modelYear);
        assertEquals(2022, modelYear.yearModel);
        assertEquals("Gasolina", modelYear.fuelName);
        assertEquals("GAS", modelYear.fuelCode);
        assertEquals("038003-2", modelYear.fipeCode);

        Price price = Price.find("modelYear.id = ?1", modelYear.id).firstResult();
        assertNotNull(price);
        assertEquals(new BigDecimal("45000.00"), price.value);
        assertEquals(Currency.BRL, price.currency);
        assertEquals("setembro de 2025", price.referenceMonth);
        assertEquals("abc123", price.authentication);
        assertNotNull(price.consultedAt);
    }

    @Test
    @Transactional
    void testProcessFipeData_WithTrucks() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.trucks = new ArrayList<>();
        
        FipeDataDtos.Truck truck = new FipeDataDtos.Truck();
        truck.id = 2L;
        truck.name = "Volvo";
        truck.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 201L;
        model.name = "FH 540";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "123456-1";
        year.modelYear = "2023 Diesel";
        year.authentication = "def456";
        year.queryDate = "terça-feira, 2 de setembro de 2025 10:00";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("350000.00");
        averagePrice.formattedValue = "R$ 350.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        truck.models.add(model);
        request.trucks.add(truck);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType vehicleType = VehicleType.find("name = ?1", "Caminhões").firstResult();
        assertNotNull(vehicleType);
        
        Brand brand = Brand.find("vehicleType.id = ?1 and externalCode = ?2", 
                vehicleType.id, "2").firstResult();
        assertNotNull(brand);
        assertEquals("Volvo", brand.name);

        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        assertNotNull(modelEntity);
        assertEquals("FH 540", modelEntity.name);

        ModelYear modelYear = ModelYear.find("model.id = ?1", modelEntity.id).firstResult();
        assertNotNull(modelYear);
        assertEquals(2023, modelYear.yearModel);
        assertEquals("Diesel", modelYear.fuelName);
        assertEquals("DIE", modelYear.fuelCode);
    }

    @Test
    @Transactional
    void testProcessFipeData_WithMotorCycles() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.motorCycles = new ArrayList<>();
        
        FipeDataDtos.MotorCycle motorCycle = new FipeDataDtos.MotorCycle();
        motorCycle.id = 3L;
        motorCycle.name = "Honda";
        motorCycle.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 301L;
        model.name = "CB 600F";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "789012-3";
        year.modelYear = "2021 Gasolina";
        year.authentication = "ghi789";
        year.queryDate = "terça-feira, 2 de setembro de 2025 11:00";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("35000.00");
        averagePrice.formattedValue = "R$ 35.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        motorCycle.models.add(model);
        request.motorCycles.add(motorCycle);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType vehicleType = VehicleType.find("name = ?1", "Motocicletas").firstResult();
        assertNotNull(vehicleType);
        
        Brand brand = Brand.find("vehicleType.id = ?1 and externalCode = ?2", 
                vehicleType.id, "3").firstResult();
        assertNotNull(brand);
        assertEquals("Honda", brand.name);

        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        assertNotNull(modelEntity);
        assertEquals("CB 600F", modelEntity.name);
    }

    @Test
    @Transactional
    void testProcessFipeData_WithMultipleVehicleTypes() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        
        // Carros
        request.cars = new ArrayList<>();
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        request.cars.add(car);
        
        // Caminhões
        request.trucks = new ArrayList<>();
        FipeDataDtos.Truck truck = new FipeDataDtos.Truck();
        truck.id = 2L;
        truck.name = "Volvo";
        truck.models = new ArrayList<>();
        request.trucks.add(truck);
        
        // Motocicletas
        request.motorCycles = new ArrayList<>();
        FipeDataDtos.MotorCycle motorCycle = new FipeDataDtos.MotorCycle();
        motorCycle.id = 3L;
        motorCycle.name = "Honda";
        motorCycle.models = new ArrayList<>();
        request.motorCycles.add(motorCycle);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType carros = VehicleType.find("name = ?1", "Carros").firstResult();
        VehicleType caminhoes = VehicleType.find("name = ?1", "Caminhões").firstResult();
        VehicleType motocicletas = VehicleType.find("name = ?1", "Motocicletas").firstResult();
        
        assertNotNull(carros);
        assertNotNull(caminhoes);
        assertNotNull(motocicletas);
        
        Brand fiat = Brand.find("vehicleType.id = ?1 and externalCode = ?2", carros.id, "1").firstResult();
        Brand volvo = Brand.find("vehicleType.id = ?1 and externalCode = ?2", caminhoes.id, "2").firstResult();
        Brand honda = Brand.find("vehicleType.id = ?1 and externalCode = ?2", motocicletas.id, "3").firstResult();
        
        assertNotNull(fiat);
        assertNotNull(volvo);
        assertNotNull(honda);
    }

    @Test
    @Transactional
    void testProcessFipeData_UpdateExistingBrand() {
        // Arrange - Criar marca existente
        VehicleType vehicleType = new VehicleType();
        vehicleType.name = "Carros";
        vehicleType.persist();
        
        Brand existingBrand = new Brand();
        existingBrand.vehicleType = vehicleType;
        existingBrand.externalCode = "1";
        existingBrand.name = "Fiat Antiga";
        existingBrand.persist();
        
        // Criar request com mesmo externalCode mas nome diferente
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat Nova";
        car.models = new ArrayList<>();
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        Brand brand = Brand.findById(existingBrand.id);
        assertNotNull(brand);
        assertEquals("Fiat Nova", brand.name); // Nome deve ser atualizado
    }

    @Test
    @Transactional
    void testProcessFipeData_UpdateExistingPrice() {
        // Arrange - Criar estrutura existente
        VehicleType vehicleType = new VehicleType();
        vehicleType.name = "Carros";
        vehicleType.persist();
        
        Brand brand = new Brand();
        brand.vehicleType = vehicleType;
        brand.externalCode = "1";
        brand.name = "Fiat";
        brand.persist();
        
        Model model = new Model();
        model.brand = brand;
        model.name = "Uno 1.0";
        model.fipeCode = "038003";
        model.persist();
        
        ModelYear modelYear = new ModelYear();
        modelYear.model = model;
        modelYear.yearModel = 2022;
        modelYear.fuelCode = "GAS";
        modelYear.fuelName = "Gasolina";
        modelYear.yearCode = "2022 Gasolina";
        modelYear.fipeCode = "038003-2";
        modelYear.authentication = "old123";
        modelYear.persist();
        
        Price existingPrice = new Price();
        existingPrice.modelYear = modelYear;
        existingPrice.referenceMonth = "setembro de 2025";
        existingPrice.value = new BigDecimal("40000.00");
        existingPrice.currency = Currency.BRL;
        existingPrice.authentication = "old123";
        existingPrice.consultedAt = LocalDateTime.now().minusDays(1);
        existingPrice.persist();
        
        // Criar request com novo preço
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model modelDto = new FipeDataDtos.Model();
        modelDto.id = 101L;
        modelDto.name = "Uno 1.0";
        modelDto.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "038003-2";
        year.modelYear = "2022 Gasolina";
        year.authentication = "new456";
        year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("45000.00");
        averagePrice.formattedValue = "R$ 45.000,00";
        year.averagePrice = averagePrice;
        
        modelDto.years.add(year);
        car.models.add(modelDto);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        Price price = Price.findById(existingPrice.id);
        assertNotNull(price);
        assertEquals(new BigDecimal("45000.00"), price.value); // Preço atualizado
        assertEquals("new456", price.authentication); // Authentication atualizado
    }

    @Test
    @Transactional
    void testProcessFipeData_FuelCodeGeneration() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno";
        model.years = new ArrayList<>();
        
        // Testar diferentes tipos de combustível
        String[] fuelTypes = {"Gasolina", "Diesel", "Etanol", "Flex", "Elétrico"};
        String[] expectedCodes = {"GAS", "DIE", "ETA", "FLE", "ELE"};
        
        for (int i = 0; i < fuelTypes.length; i++) {
            FipeDataDtos.Year year = new FipeDataDtos.Year();
            year.referenceMonth = "setembro de 2025";
            year.fipeCode = "03800" + i;
            year.modelYear = "2022 " + fuelTypes[i];
            year.authentication = "auth" + i;
            year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
            
            FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
            averagePrice.value = new BigDecimal("40000.00");
            averagePrice.formattedValue = "R$ 40.000,00";
            year.averagePrice = averagePrice;
            
            model.years.add(year);
        }
        
        car.models.add(model);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        List<ModelYear> modelYears = ModelYear.find("model.id = ?1", modelEntity.id).list();
        
        assertEquals(fuelTypes.length, modelYears.size());
        for (int i = 0; i < fuelTypes.length; i++) {
            final int index = i; // Tornar final para uso em lambda
            final String fuelType = fuelTypes[i];
            final String expectedCode = expectedCodes[i];
            ModelYear modelYear = modelYears.stream()
                    .filter(my -> my.fuelName.equals(fuelType))
                    .findFirst()
                    .orElse(null);
            assertNotNull(modelYear, "ModelYear não encontrado para " + fuelType);
            assertEquals(expectedCode, modelYear.fuelCode);
        }
    }

    @Test
    @Transactional
    void testProcessFipeData_ExtractBaseFipeCode() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "038003-2"; // Com sufixo
        year.modelYear = "2022 Gasolina";
        year.authentication = "abc123";
        year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("45000.00");
        averagePrice.formattedValue = "R$ 45.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        car.models.add(model);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert - O Model deve ter o fipeCode base sem sufixo
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        
        assertEquals("038003", modelEntity.fipeCode); // Base sem sufixo "-2"
        
        // O ModelYear deve ter o fipeCode completo
        ModelYear modelYear = ModelYear.find("model.id = ?1", modelEntity.id).firstResult();
        assertEquals("038003-2", modelYear.fipeCode); // Completo com sufixo
    }

    @Test
    @Transactional
    void testProcessFipeData_ModelVersionParsing() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        // Testar diferentes formatos de nomes de modelos
        String[] modelNames = {"Integra GS 1.8", "Legend 3.2/3.5", "100 2.8 V6"};
        
        for (int i = 0; i < modelNames.length; i++) {
            FipeDataDtos.Model model = new FipeDataDtos.Model();
            model.id = 100L + i;
            model.name = modelNames[i];
            model.years = new ArrayList<>();
            
            FipeDataDtos.Year year = new FipeDataDtos.Year();
            year.referenceMonth = "setembro de 2025";
            year.fipeCode = "03800" + i;
            year.modelYear = "2022 Gasolina";
            year.authentication = "auth" + i;
            year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
            
            FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
            averagePrice.value = new BigDecimal("40000.00");
            averagePrice.formattedValue = "R$ 40.000,00";
            year.averagePrice = averagePrice;
            
            model.years.add(year);
            car.models.add(model);
        }
        
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert - Verificar que os modelos foram criados e têm model/version separados
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        List<Model> models = Model.find("brand.id = ?1", brand.id).list();
        
        assertEquals(modelNames.length, models.size());
        // Verificar que pelo menos alguns têm model/version separados
        boolean hasParsedModel = models.stream()
                .anyMatch(m -> m.model != null && !m.model.isEmpty());
        assertTrue(hasParsedModel, "Pelo menos um modelo deveria ter sido parseado");
    }

    @Test
    @Transactional
    void testProcessFipeData_YearCodeTruncation() {
        // Arrange - Criar um modelYear com nome muito longo
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "038003-2";
        year.modelYear = "2022 Gasolina com descrição muito longa que deve ser truncada";
        year.authentication = "abc123";
        year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("45000.00");
        averagePrice.formattedValue = "R$ 45.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        car.models.add(model);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert - yearCode deve ser truncado para 16 caracteres
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        ModelYear modelYear = ModelYear.find("model.id = ?1", modelEntity.id).firstResult();
        
        assertNotNull(modelYear);
        assertTrue(modelYear.yearCode.length() <= 16, 
                "yearCode deve ter no máximo 16 caracteres, mas tem " + modelYear.yearCode.length());
    }

    @Test
    @Transactional
    void testProcessFipeData_InvalidQueryDate() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "038003-2";
        year.modelYear = "2022 Gasolina";
        year.authentication = "abc123";
        year.queryDate = "data inválida"; // Data inválida
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("45000.00");
        averagePrice.formattedValue = "R$ 45.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        car.models.add(model);
        request.cars.add(car);

        // Act
        LocalDateTime beforeProcessing = LocalDateTime.now();
        fipeDataService.processFipeData(request);
        LocalDateTime afterProcessing = LocalDateTime.now();

        // Assert - Deve usar data atual como fallback
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        ModelYear modelYear = ModelYear.find("model.id = ?1", modelEntity.id).firstResult();
        Price price = Price.find("modelYear.id = ?1", modelYear.id).firstResult();
        
        assertNotNull(price.consultedAt);
        // Deve estar entre beforeProcessing e afterProcessing (com margem de alguns segundos)
        assertTrue(price.consultedAt.isAfter(beforeProcessing.minusSeconds(5)) && 
                   price.consultedAt.isBefore(afterProcessing.plusSeconds(5)));
    }

    @Test
    @Transactional
    void testProcessFipeData_ModelYearWithoutPattern() {
        // Arrange - modelYear que não segue o padrão "YYYY Combustível"
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno";
        model.years = new ArrayList<>();
        
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "038003-2";
        year.modelYear = "Formato Inválido"; // Não segue o padrão
        year.authentication = "abc123";
        year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
        
        FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
        averagePrice.value = new BigDecimal("45000.00");
        averagePrice.formattedValue = "R$ 45.000,00";
        year.averagePrice = averagePrice;
        
        model.years.add(year);
        car.models.add(model);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert - Deve usar fallback (yearModel = 0, fuelCode = "UNK")
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        ModelYear modelYear = ModelYear.find("model.id = ?1", modelEntity.id).firstResult();
        
        assertNotNull(modelYear);
        assertEquals(0, modelYear.yearModel); // Fallback
        assertEquals("UNK", modelYear.fuelCode); // Fallback
        assertEquals("Formato Inválido", modelYear.fuelName);
    }

    @Test
    @Transactional
    void testProcessFipeData_MultipleModelsSameBrand() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        // Criar múltiplos modelos
        for (int i = 0; i < 3; i++) {
            FipeDataDtos.Model model = new FipeDataDtos.Model();
            model.id = 100L + i;
            model.name = "Modelo " + i;
            model.years = new ArrayList<>();
            
            FipeDataDtos.Year year = new FipeDataDtos.Year();
            year.referenceMonth = "setembro de 2025";
            year.fipeCode = "03800" + i;
            year.modelYear = "2022 Gasolina";
            year.authentication = "auth" + i;
            year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
            
            FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
            averagePrice.value = new BigDecimal("40000.00");
            averagePrice.formattedValue = "R$ 40.000,00";
            year.averagePrice = averagePrice;
            
            model.years.add(year);
            car.models.add(model);
        }
        
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        List<Model> models = Model.find("brand.id = ?1", brand.id).list();
        
        assertEquals(3, models.size());
    }

    @Test
    @Transactional
    void testProcessFipeData_MultipleYearsSameModel() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 101L;
        model.name = "Uno";
        model.years = new ArrayList<>();
        
        // Criar múltiplos anos
        for (int i = 0; i < 3; i++) {
            FipeDataDtos.Year year = new FipeDataDtos.Year();
            year.referenceMonth = "setembro de 2025";
            year.fipeCode = "038003-" + i;
            year.modelYear = (2020 + i) + " Gasolina";
            year.authentication = "auth" + i;
            year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
            
            FipeDataDtos.AveragePrice averagePrice = new FipeDataDtos.AveragePrice();
            averagePrice.value = new BigDecimal("40000.00");
            averagePrice.formattedValue = "R$ 40.000,00";
            year.averagePrice = averagePrice;
            
            model.years.add(year);
        }
        
        car.models.add(model);
        request.cars.add(car);

        // Act
        fipeDataService.processFipeData(request);

        // Assert
        VehicleType vehicleType = VehicleType.find("name = ?1", "Carros").firstResult();
        Brand brand = Brand.find("vehicleType.id = ?1", vehicleType.id).firstResult();
        Model modelEntity = Model.find("brand.id = ?1", brand.id).firstResult();
        List<ModelYear> modelYears = ModelYear.find("model.id = ?1", modelEntity.id).list();
        
        assertEquals(3, modelYears.size());
        // Verificar que cada ano foi criado corretamente
        for (int i = 0; i < 3; i++) {
            int expectedYear = 2020 + i;
            ModelYear modelYear = modelYears.stream()
                    .filter(my -> my.yearModel == expectedYear)
                    .findFirst()
                    .orElse(null);
            assertNotNull(modelYear, "ModelYear não encontrado para ano " + expectedYear);
            assertEquals("038003-" + i, modelYear.fipeCode);
        }
    }
}

