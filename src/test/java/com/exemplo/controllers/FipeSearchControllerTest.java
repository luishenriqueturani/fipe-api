package com.exemplo.controllers;

import com.exemplo.dto.FipeSearchDtos;
import com.exemplo.services.FipeSearchService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FipeSearchControllerTest {

    @Mock
    private FipeSearchService fipeSearchService;

    @InjectMocks
    private FipeSearchController fipeSearchController;

    @Test
    void testSearchVehicleTypes_Success() {
        // Arrange
        String name = "Carro";
        int page = 1;
        int pageSize = 10;

        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> mockResponse = 
                createMockVehicleTypeResponse(page, pageSize, 1);

        when(fipeSearchService.searchVehicleTypes(name, page, pageSize)).thenReturn(mockResponse);

        // Act
        Response response = fipeSearchController.searchVehicleTypes(name, page, pageSize);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        verify(fipeSearchService, times(1)).searchVehicleTypes(name, page, pageSize);
    }

    @Test
    void testSearchVehicleTypes_NullName() {
        // Act
        Response response = fipeSearchController.searchVehicleTypes(null, 1, 10);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'name' é obrigatório"));
        
        verify(fipeSearchService, never()).searchVehicleTypes(anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchVehicleTypes_EmptyName() {
        // Act
        Response response = fipeSearchController.searchVehicleTypes("   ", 1, 10);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'name' é obrigatório"));
        
        verify(fipeSearchService, never()).searchVehicleTypes(anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchVehicleTypes_InvalidPage() {
        // Act
        Response response = fipeSearchController.searchVehicleTypes("Carro", 0, 10);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'page' deve ser maior ou igual a 1"));
        
        verify(fipeSearchService, never()).searchVehicleTypes(anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchVehicleTypes_InvalidPageSize_TooSmall() {
        // Act
        Response response = fipeSearchController.searchVehicleTypes("Carro", 1, 0);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'pageSize' deve estar entre 1 e 100"));
        
        verify(fipeSearchService, never()).searchVehicleTypes(anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchVehicleTypes_InvalidPageSize_TooLarge() {
        // Act
        Response response = fipeSearchController.searchVehicleTypes("Carro", 1, 101);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'pageSize' deve estar entre 1 e 100"));
        
        verify(fipeSearchService, never()).searchVehicleTypes(anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchVehicleTypes_ServiceThrowsException() {
        // Arrange
        String name = "Carro";
        when(fipeSearchService.searchVehicleTypes(name, 1, 10))
                .thenThrow(new RuntimeException("Erro ao buscar"));

        // Act
        Response response = fipeSearchController.searchVehicleTypes(name, 1, 10);

        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Erro interno do servidor"));
    }

    @Test
    void testSearchBrands_Success() {
        // Arrange
        String name = "Fiat";
        String vehicleType = "Carros";
        int page = 1;
        int pageSize = 10;

        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> mockResponse = 
                createMockBrandResponse(page, pageSize, 1);

        when(fipeSearchService.searchBrands(name, vehicleType, page, pageSize)).thenReturn(mockResponse);

        // Act
        Response response = fipeSearchController.searchBrands(name, vehicleType, page, pageSize);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        verify(fipeSearchService, times(1)).searchBrands(name, vehicleType, page, pageSize);
    }

    @Test
    void testSearchBrands_WithoutVehicleType() {
        // Arrange
        String name = "Fiat";
        int page = 1;
        int pageSize = 10;

        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> mockResponse = 
                createMockBrandResponse(page, pageSize, 1);

        when(fipeSearchService.searchBrands(name, null, page, pageSize)).thenReturn(mockResponse);

        // Act
        Response response = fipeSearchController.searchBrands(name, null, page, pageSize);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(fipeSearchService, times(1)).searchBrands(name, null, page, pageSize);
    }

    @Test
    void testSearchBrands_NullName() {
        // Act
        Response response = fipeSearchController.searchBrands(null, "Carros", 1, 10);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'name' é obrigatório"));
        
        verify(fipeSearchService, never()).searchBrands(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchModels_Success() {
        // Arrange
        String name = "Uno";
        String brand = "Fiat";
        String vehicleType = "Carros";
        String modelBase = "Uno";
        String version = "1.0";
        int page = 1;
        int pageSize = 10;

        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> mockResponse = 
                createMockModelResponse(page, pageSize, 1);

        when(fipeSearchService.searchModels(name, brand, vehicleType, modelBase, version, page, pageSize))
                .thenReturn(mockResponse);

        // Act
        Response response = fipeSearchController.searchModels(name, brand, vehicleType, modelBase, version, page, pageSize);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        verify(fipeSearchService, times(1)).searchModels(name, brand, vehicleType, modelBase, version, page, pageSize);
    }

    @Test
    void testSearchModels_WithPartialFilters() {
        // Arrange
        String name = "Uno";
        String brand = "Fiat";
        int page = 1;
        int pageSize = 10;

        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> mockResponse = 
                createMockModelResponse(page, pageSize, 1);

        when(fipeSearchService.searchModels(name, brand, null, null, null, page, pageSize))
                .thenReturn(mockResponse);

        // Act
        Response response = fipeSearchController.searchModels(name, brand, null, null, null, page, pageSize);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(fipeSearchService, times(1)).searchModels(name, brand, null, null, null, page, pageSize);
    }

    @Test
    void testSearchModels_NullName() {
        // Act
        Response response = fipeSearchController.searchModels(null, "Fiat", null, null, null, 1, 10);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Parâmetro 'name' é obrigatório"));
        
        verify(fipeSearchService, never()).searchModels(anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void testSearchModels_DefaultPageAndPageSize() {
        // Arrange
        String name = "Uno";
        FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> mockResponse = 
                createMockModelResponse(1, 10, 1);

        when(fipeSearchService.searchModels(eq(name), isNull(), isNull(), isNull(), isNull(), eq(1), eq(10)))
                .thenReturn(mockResponse);

        // Act
        Response response = fipeSearchController.searchModels(name, null, null, null, null, 1, 10);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(fipeSearchService, times(1)).searchModels(name, null, null, null, null, 1, 10);
    }

    // Helper methods
    private FipeSearchDtos.PaginatedResponse<FipeSearchDtos.VehicleTypeResponse> createMockVehicleTypeResponse(
            int page, int pageSize, long totalItems) {
        List<FipeSearchDtos.VehicleTypeResponse> items = new ArrayList<>();
        FipeSearchDtos.VehicleTypeResponse item = new FipeSearchDtos.VehicleTypeResponse();
        item.id = 1L;
        item.name = "Carros";
        items.add(item);

        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(page, pageSize, totalItems);
        return new FipeSearchDtos.PaginatedResponse<>(items, meta);
    }

    private FipeSearchDtos.PaginatedResponse<FipeSearchDtos.BrandResponse> createMockBrandResponse(
            int page, int pageSize, long totalItems) {
        List<FipeSearchDtos.BrandResponse> items = new ArrayList<>();
        FipeSearchDtos.BrandResponse item = new FipeSearchDtos.BrandResponse();
        item.id = 1L;
        item.name = "Fiat";
        items.add(item);

        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(page, pageSize, totalItems);
        return new FipeSearchDtos.PaginatedResponse<>(items, meta);
    }

    private FipeSearchDtos.PaginatedResponse<FipeSearchDtos.ModelResponse> createMockModelResponse(
            int page, int pageSize, long totalItems) {
        List<FipeSearchDtos.ModelResponse> items = new ArrayList<>();
        FipeSearchDtos.ModelResponse item = new FipeSearchDtos.ModelResponse();
        item.id = 1L;
        item.name = "Uno 1.0";
        items.add(item);

        FipeSearchDtos.PaginationMeta meta = new FipeSearchDtos.PaginationMeta(page, pageSize, totalItems);
        return new FipeSearchDtos.PaginatedResponse<>(items, meta);
    }
}

