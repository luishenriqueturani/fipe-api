package com.exemplo.controllers;

import com.exemplo.dto.FipeDataDtos;
import com.exemplo.services.FipeDataService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FipeDataControllerTest {

    @Mock
    private FipeDataService fipeDataService;

    @InjectMocks
    private FipeDataController fipeDataController;

    @Test
    void testUpdateFipeData_Success() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        request.cars.add(car);

        doNothing().when(fipeDataService).processFipeData(any(FipeDataDtos.FipeDataRequest.class));

        // Act
        Response response = fipeDataController.updateFipeData(request);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Dados da FIPE atualizados com sucesso"));
        
        verify(fipeDataService, times(1)).processFipeData(request);
    }

    @Test
    void testUpdateFipeData_NullRequest() {
        // Act
        Response response = fipeDataController.updateFipeData(null);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Dados da FIPE não fornecidos"));
        
        verify(fipeDataService, never()).processFipeData(any());
    }

    @Test
    void testUpdateFipeData_EmptyVehicleTypes() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        request.trucks = new ArrayList<>();
        request.motorCycles = new ArrayList<>();

        // Act
        Response response = fipeDataController.updateFipeData(request);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Pelo menos um tipo de veículo deve ser fornecido"));
        
        verify(fipeDataService, never()).processFipeData(any());
    }

    @Test
    void testUpdateFipeData_WithCars() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        request.cars.add(car);
        request.trucks = null;
        request.motorCycles = null;

        doNothing().when(fipeDataService).processFipeData(any(FipeDataDtos.FipeDataRequest.class));

        // Act
        Response response = fipeDataController.updateFipeData(request);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(fipeDataService, times(1)).processFipeData(request);
    }

    @Test
    void testUpdateFipeData_WithTrucks() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = null;
        request.trucks = new ArrayList<>();
        FipeDataDtos.Truck truck = new FipeDataDtos.Truck();
        truck.id = 1L;
        truck.name = "Volvo";
        truck.models = new ArrayList<>();
        request.trucks.add(truck);
        request.motorCycles = null;

        doNothing().when(fipeDataService).processFipeData(any(FipeDataDtos.FipeDataRequest.class));

        // Act
        Response response = fipeDataController.updateFipeData(request);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(fipeDataService, times(1)).processFipeData(request);
    }

    @Test
    void testUpdateFipeData_WithMotorCycles() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = null;
        request.trucks = null;
        request.motorCycles = new ArrayList<>();
        FipeDataDtos.MotorCycle motorCycle = new FipeDataDtos.MotorCycle();
        motorCycle.id = 1L;
        motorCycle.name = "Honda";
        motorCycle.models = new ArrayList<>();
        request.motorCycles.add(motorCycle);

        doNothing().when(fipeDataService).processFipeData(any(FipeDataDtos.FipeDataRequest.class));

        // Act
        Response response = fipeDataController.updateFipeData(request);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(fipeDataService, times(1)).processFipeData(request);
    }

    @Test
    void testUpdateFipeData_ServiceThrowsException() {
        // Arrange
        FipeDataDtos.FipeDataRequest request = new FipeDataDtos.FipeDataRequest();
        request.cars = new ArrayList<>();
        FipeDataDtos.Car car = new FipeDataDtos.Car();
        car.id = 1L;
        car.name = "Fiat";
        car.models = new ArrayList<>();
        request.cars.add(car);

        doThrow(new RuntimeException("Erro ao processar dados")).when(fipeDataService)
                .processFipeData(any(FipeDataDtos.FipeDataRequest.class));

        // Act
        Response response = fipeDataController.updateFipeData(request);

        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Erro interno do servidor"));
        
        verify(fipeDataService, times(1)).processFipeData(request);
    }
}

