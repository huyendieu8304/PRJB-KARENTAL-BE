package com.mp.karental.service;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.repository.CarRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CarServiceTest {
    @Autowired
    private CarService carService;
    @MockitoBean
    private CarRepository carRepository;
    private AddCarRequest addCarRequest;
    private CarResponse carResponse;
    private Car car;
    @BeforeEach
    void setUp() {
        // Given: Set up a mock AddCarRequest
        addCarRequest = new AddCarRequest();
        addCarRequest.setLicensePlate("49F-123.45");
        addCarRequest.setBrand("Toyota");
        addCarRequest.setModel("Camry");
        addCarRequest.setColor("Black");
        addCarRequest.setNumberOfSeats(5);
        addCarRequest.setProductionYear(2020);
        addCarRequest.setMileage(15000);
        addCarRequest.setFuelConsumption(7.5f);
        addCarRequest.setBasePrice(50000);
        addCarRequest.setDeposit(500000);
        addCarRequest.setAddress("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng");
        addCarRequest.setDescription("This is a test");
        addCarRequest.setAdditionalFunction("Bluetooth");
        addCarRequest.setTermOfUse("No");
        addCarRequest.setAutomatic(true);
        addCarRequest.setGasoline(true);
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.pdf", "application/octet-stream", "fake content".getBytes());
        addCarRequest.setRegistrationPaper(emptyFile);
        addCarRequest.setCertificateOfInspection(emptyFile);
        addCarRequest.setInsurance(emptyFile);
        addCarRequest.setCarImageBack(emptyFile);
        addCarRequest.setCarImageFront(emptyFile);
        addCarRequest.setCarImageLeft(emptyFile);
        addCarRequest.setCarImageRight(emptyFile);
    }
    @Test
    void addCar_validRequest_success() throws AppException {
        // Given
        Car car = new Car();
        car.setLicensePlate("49F-123.45");

        when(carRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(carRepository.save(any(Car.class))).thenReturn(car);

        // When
        var response = carService.addNewCar(addCarRequest);

        // Debugging log
        System.out.println("Response: " + response);
        Assertions.assertNotNull(response, "Response should not be null");

        // Then
        Assertions.assertEquals("49F-123.45", response.getLicensePlate());
    }
}
