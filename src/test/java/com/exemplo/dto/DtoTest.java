package com.exemplo.dto;

import com.exemplo.enums.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    // ========== AuthDtos Tests ==========

    @Test
    void testTokenRequest_Structure() {
        // Arrange & Act
        AuthDtos.TokenRequest request = new AuthDtos.TokenRequest();
        request.email = "test@example.com";
        request.password = "password123";

        // Assert
        assertEquals("test@example.com", request.email);
        assertEquals("password123", request.password);
    }

    @Test
    void testAdminLoginRequest_Structure() {
        // Arrange & Act
        AuthDtos.AdminLoginRequest request = new AuthDtos.AdminLoginRequest();
        request.email = "admin@example.com";
        request.password = "admin123";

        // Assert
        assertEquals("admin@example.com", request.email);
        assertEquals("admin123", request.password);
    }

    @Test
    void testTokenResponse_DefaultConstructor() {
        // Arrange & Act
        AuthDtos.TokenResponse response = new AuthDtos.TokenResponse();

        // Assert
        assertNull(response.access_token);
        assertNull(response.token_type);
        assertEquals(0, response.expires_in);
    }

    @Test
    void testTokenResponse_ParameterizedConstructor() {
        // Arrange & Act
        AuthDtos.TokenResponse response = new AuthDtos.TokenResponse(
                "token123", "Bearer", 3600L);

        // Assert
        assertEquals("token123", response.access_token);
        assertEquals("Bearer", response.token_type);
        assertEquals(3600L, response.expires_in);
    }

    // ========== SessionDtos Tests ==========

    @Test
    void testSessionResponse_Structure() {
        // Arrange & Act
        SessionDtos.SessionResponse response = new SessionDtos.SessionResponse();
        LocalDateTime now = LocalDateTime.now();
        response.id = 1L;
        response.tokenJti = "jti123";
        response.loginAt = now;
        response.lastActivityAt = now;
        response.expiresAt = now.plusHours(1);
        response.isActive = true;
        response.ipAddress = "127.0.0.1";
        response.userAgent = "Mozilla/5.0";

        // Assert
        assertEquals(1L, response.id);
        assertEquals("jti123", response.tokenJti);
        assertEquals(now, response.loginAt);
        assertEquals(now, response.lastActivityAt);
        assertEquals(now.plusHours(1), response.expiresAt);
        assertTrue(response.isActive);
        assertEquals("127.0.0.1", response.ipAddress);
        assertEquals("Mozilla/5.0", response.userAgent);
    }

    @Test
    void testClientInfo_Structure() {
        // Arrange & Act
        SessionDtos.ClientInfo clientInfo = new SessionDtos.ClientInfo();
        clientInfo.id = 1L;
        clientInfo.name = "Test Client";
        clientInfo.clientId = "client-123";

        // Assert
        assertEquals(1L, clientInfo.id);
        assertEquals("Test Client", clientInfo.name);
        assertEquals("client-123", clientInfo.clientId);
    }

    @Test
    void testAdminInfo_Structure() {
        // Arrange & Act
        SessionDtos.AdminInfo adminInfo = new SessionDtos.AdminInfo();
        adminInfo.id = 1L;
        adminInfo.name = "Admin User";
        adminInfo.username = "admin";
        adminInfo.email = "admin@example.com";

        // Assert
        assertEquals(1L, adminInfo.id);
        assertEquals("Admin User", adminInfo.name);
        assertEquals("admin", adminInfo.username);
        assertEquals("admin@example.com", adminInfo.email);
    }

    // ========== FipeSearchDtos.PaginationMeta Tests ==========

    @Test
    void testPaginationMeta_FirstPage() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(1, 10, 25);

        // Assert
        assertEquals(1, meta.page);
        assertEquals(10, meta.pageSize);
        assertEquals(25, meta.totalItems);
        assertEquals(3, meta.totalPages); // 25 / 10 = 2.5, arredondado para 3
        assertTrue(meta.hasNext);
        assertFalse(meta.hasPrevious);
    }

    @Test
    void testPaginationMeta_LastPage() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(3, 10, 25);

        // Assert
        assertEquals(3, meta.page);
        assertEquals(10, meta.pageSize);
        assertEquals(25, meta.totalItems);
        assertEquals(3, meta.totalPages);
        assertFalse(meta.hasNext);
        assertTrue(meta.hasPrevious);
    }

    @Test
    void testPaginationMeta_MiddlePage() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(2, 10, 25);

        // Assert
        assertEquals(2, meta.page);
        assertEquals(10, meta.pageSize);
        assertEquals(25, meta.totalItems);
        assertEquals(3, meta.totalPages);
        assertTrue(meta.hasNext);
        assertTrue(meta.hasPrevious);
    }

    @Test
    void testPaginationMeta_ExactDivision() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(2, 10, 20);

        // Assert
        assertEquals(2, meta.page);
        assertEquals(10, meta.pageSize);
        assertEquals(20, meta.totalItems);
        assertEquals(2, meta.totalPages);
        assertFalse(meta.hasNext);
        assertTrue(meta.hasPrevious);
    }

    @Test
    void testPaginationMeta_EmptyResults() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(1, 10, 0);

        // Assert
        assertEquals(1, meta.page);
        assertEquals(10, meta.pageSize);
        assertEquals(0, meta.totalItems);
        assertEquals(0, meta.totalPages);
        assertFalse(meta.hasNext);
        assertFalse(meta.hasPrevious);
    }

    @Test
    void testPaginationMeta_SinglePage() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(1, 10, 5);

        // Assert
        assertEquals(1, meta.page);
        assertEquals(10, meta.pageSize);
        assertEquals(5, meta.totalItems);
        assertEquals(1, meta.totalPages);
        assertFalse(meta.hasNext);
        assertFalse(meta.hasPrevious);
    }

    @Test
    void testPaginationMeta_LargePageSize() {
        // Arrange & Act
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(1, 100, 50);

        // Assert
        assertEquals(1, meta.page);
        assertEquals(100, meta.pageSize);
        assertEquals(50, meta.totalItems);
        assertEquals(1, meta.totalPages);
        assertFalse(meta.hasNext);
        assertFalse(meta.hasPrevious);
    }

    // ========== FipeSearchDtos.PaginatedResponse Tests ==========

    @Test
    void testPaginatedResponse_Structure() {
        // Arrange
        List<String> data = List.of("item1", "item2");
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(1, 10, 2);

        // Act
        FipeSearchDtos.PaginatedResponse<String> response = 
                new FipeSearchDtos.PaginatedResponse<>(data, meta);

        // Assert
        assertEquals(2, response.data.size());
        assertEquals("item1", response.data.get(0));
        assertEquals("item2", response.data.get(1));
        assertEquals(meta, response.meta);
    }

    @Test
    void testPaginatedResponse_EmptyData() {
        // Arrange
        List<String> data = new ArrayList<>();
        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(1, 10, 0);

        // Act
        FipeSearchDtos.PaginatedResponse<String> response = 
                new FipeSearchDtos.PaginatedResponse<>(data, meta);

        // Assert
        assertTrue(response.data.isEmpty());
        assertEquals(meta, response.meta);
    }

    // ========== FipeSearchDtos Response Classes Tests ==========

    @Test
    void testVehicleTypeResponse_Structure() {
        // Arrange & Act
        FipeSearchDtos.VehicleTypeResponse response = new FipeSearchDtos.VehicleTypeResponse();
        LocalDateTime now = LocalDateTime.now();
        response.id = 1L;
        response.name = "Carros";
        response.createdAt = now;
        response.updatedAt = now;
        response.brands = new ArrayList<>();

        // Assert
        assertEquals(1L, response.id);
        assertEquals("Carros", response.name);
        assertEquals(now, response.createdAt);
        assertEquals(now, response.updatedAt);
        assertNotNull(response.brands);
    }

    @Test
    void testBrandResponse_Structure() {
        // Arrange & Act
        FipeSearchDtos.BrandResponse response = new FipeSearchDtos.BrandResponse();
        LocalDateTime now = LocalDateTime.now();
        response.id = 1L;
        response.name = "Fiat";
        response.externalCode = "21";
        response.createdAt = now;
        response.updatedAt = now;
        response.models = new ArrayList<>();

        // Assert
        assertEquals(1L, response.id);
        assertEquals("Fiat", response.name);
        assertEquals("21", response.externalCode);
        assertEquals(now, response.createdAt);
        assertEquals(now, response.updatedAt);
        assertNotNull(response.models);
    }

    @Test
    void testModelResponse_Structure() {
        // Arrange & Act
        FipeSearchDtos.ModelResponse response = new FipeSearchDtos.ModelResponse();
        LocalDateTime now = LocalDateTime.now();
        response.id = 1L;
        response.name = "Uno";
        response.model = "Uno";
        response.version = "1.0";
        response.fipeCode = "001001";
        response.createdAt = now;
        response.updatedAt = now;
        response.years = new ArrayList<>();

        // Assert
        assertEquals(1L, response.id);
        assertEquals("Uno", response.name);
        assertEquals("Uno", response.model);
        assertEquals("1.0", response.version);
        assertEquals("001001", response.fipeCode);
        assertEquals(now, response.createdAt);
        assertEquals(now, response.updatedAt);
        assertNotNull(response.years);
    }

    @Test
    void testModelYearResponse_Structure() {
        // Arrange & Act
        FipeSearchDtos.ModelYearResponse response = new FipeSearchDtos.ModelYearResponse();
        LocalDateTime now = LocalDateTime.now();
        response.id = 1L;
        response.yearModel = 2022;
        response.fuelCode = "GAS";
        response.fuelName = "Gasolina";
        response.yearCode = "2022-1";
        response.fipeCode = "001001-1";
        response.authentication = "auth123";
        response.createdAt = now;
        response.updatedAt = now;
        response.prices = new ArrayList<>();

        // Assert
        assertEquals(1L, response.id);
        assertEquals(2022, response.yearModel);
        assertEquals("GAS", response.fuelCode);
        assertEquals("Gasolina", response.fuelName);
        assertEquals("2022-1", response.yearCode);
        assertEquals("001001-1", response.fipeCode);
        assertEquals("auth123", response.authentication);
        assertEquals(now, response.createdAt);
        assertEquals(now, response.updatedAt);
        assertNotNull(response.prices);
    }

    @Test
    void testPriceResponse_Structure() {
        // Arrange & Act
        FipeSearchDtos.PriceResponse response = new FipeSearchDtos.PriceResponse();
        LocalDateTime now = LocalDateTime.now();
        response.id = 1L;
        response.referenceMonth = "setembro de 2025";
        response.value = new BigDecimal("35000.00");
        response.currency = Currency.BRL;
        response.authentication = "auth123";
        response.consultedAt = now;
        response.createdAt = now;
        response.updatedAt = now;

        // Assert
        assertEquals(1L, response.id);
        assertEquals("setembro de 2025", response.referenceMonth);
        assertEquals(new BigDecimal("35000.00"), response.value);
        assertEquals(Currency.BRL, response.currency);
        assertEquals("auth123", response.authentication);
        assertEquals(now, response.consultedAt);
        assertEquals(now, response.createdAt);
        assertEquals(now, response.updatedAt);
    }

    // ========== FipeDataDtos Tests ==========

    @Test
    void testFipeDataRequest_Structure() {
        // Arrange & Act
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        request.trucks = new ArrayList<>();
        request.motorCycles = new ArrayList<>();

        // Assert
        assertNotNull(request.cars);
        assertNotNull(request.trucks);
        assertNotNull(request.motorCycles);
    }

    @Test
    void testCar_Structure() {
        // Arrange & Act
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();

        // Assert
        assertEquals(1L, car.id);
        assertEquals("Fiat", car.name);
        assertNotNull(car.models);
    }

    @Test
    void testTruck_Structure() {
        // Arrange & Act
        FipeDataDtos.Truck truck = new FipeDataDtos.Truck();
        truck.id = 1L;
        truck.name = "Volvo";
        truck.models = new ArrayList<>();

        // Assert
        assertEquals(1L, truck.id);
        assertEquals("Volvo", truck.name);
        assertNotNull(truck.models);
    }

    @Test
    void testMotorCycle_Structure() {
        // Arrange & Act
        FipeDataDtos.MotorCycle motorCycle = new FipeDataDtos.MotorCycle();
        motorCycle.id = 1L;
        motorCycle.name = "Honda";
        motorCycle.models = new ArrayList<>();

        // Assert
        assertEquals(1L, motorCycle.id);
        assertEquals("Honda", motorCycle.name);
        assertNotNull(motorCycle.models);
    }

    @Test
    void testModel_Structure() {
        // Arrange & Act
        FipeDataDtos.Model model = new FipeDataDtos.Model();
        model.id = 1L;
        model.name = "Uno";
        model.years = new ArrayList<>();

        // Assert
        assertEquals(1L, model.id);
        assertEquals("Uno", model.name);
        assertNotNull(model.years);
    }

    @Test
    void testYear_Structure() {
        // Arrange & Act
        FipeDataDtos.Year year = new FipeDataDtos.Year();
        year.referenceMonth = "setembro de 2025";
        year.fipeCode = "001001-1";
        year.brand = "Fiat";
        year.model = "Uno";
        year.modelYear = "2022 Gasolina";
        year.authentication = "auth123";
        year.queryDate = "terça-feira, 2 de setembro de 2025 09:33";
        year.averagePrice = new FipeDataDtos.AveragePrice();

        // Assert
        assertEquals("setembro de 2025", year.referenceMonth);
        assertEquals("001001-1", year.fipeCode);
        assertEquals("Fiat", year.brand);
        assertEquals("Uno", year.model);
        assertEquals("2022 Gasolina", year.modelYear);
        assertEquals("auth123", year.authentication);
        assertEquals("terça-feira, 2 de setembro de 2025 09:33", year.queryDate);
        assertNotNull(year.averagePrice);
    }

    @Test
    void testAveragePrice_Structure() {
        // Arrange & Act
        FipeDataDtos.AveragePrice price = new FipeDataDtos.AveragePrice();
        price.value = new BigDecimal("35000.00");
        price.formattedValue = "R$ 35.000,00";

        // Assert
        assertEquals(new BigDecimal("35000.00"), price.value);
        assertEquals("R$ 35.000,00", price.formattedValue);
    }
}

