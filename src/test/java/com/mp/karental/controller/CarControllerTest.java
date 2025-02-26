package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.service.CarService;
import com.mp.karental.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest(classes = KarentalApplication.class)
@ExtendWith(MockitoExtension.class)
@Slf4j
@AutoConfigureMockMvc
public class CarControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;
    @MockitoBean
    private FileService fileService;

    private AddCarRequest addCarRequest;

    // Initialize the data before each test
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
    }


    @Test
    void addNewCar_Success() throws Exception {
        // Prepare expected response
        CarResponse carResponse = new CarResponse();
        carResponse.setLicensePlate("49F-123.45");
        carResponse.setBrand("Toyota");
        carResponse.setModel("Camry");
        carResponse.setColor("Black");
        carResponse.setNumberOfSeats(5);
        carResponse.setProductionYear(2020);
        carResponse.setMileage(15000);
        carResponse.setFuelConsumption(7.5f);
        carResponse.setBasePrice(50000);
        carResponse.setDeposit(500000);
        carResponse.setAddress("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng");
        carResponse.setDescription("This is a test");
        carResponse.setAdditionalFunction("Bluetooth");
        carResponse.setTermOfUse("No");
        carResponse.setAutomatic(true);
        carResponse.setGasoline(true);

        // Mock the car service behavior
        Mockito.when(carService.addNewCar(ArgumentMatchers.any())).thenReturn(carResponse);
        // Mock the files for the multipart request
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "test.properties/octet-stream", new byte[0]);

        // Perform the test: Add a new car with the JWT token for authorization
        mockMvc.perform(multipart("/car/addCar")
                        .file("registrationPaper", emptyFile.getBytes())
                        .file("certificateOfInspection", emptyFile.getBytes())
                        .file("insurance", emptyFile.getBytes())
                        .file("carImageFront", emptyFile.getBytes())
                        .file("carImageBack", emptyFile.getBytes())
                        .file("carImageLeft", emptyFile.getBytes())
                        .file("carImageRight", emptyFile.getBytes())
                        .param("brand", addCarRequest.getBrand())
                        .param("model", addCarRequest.getModel())
                        .param("address", addCarRequest.getAddress())
                        .param("licensePlate", addCarRequest.getLicensePlate())
                        .param("color", addCarRequest.getColor())
                        .param("numberOfSeats", String.valueOf(addCarRequest.getNumberOfSeats()))
                        .param("productionYear", String.valueOf(addCarRequest.getProductionYear()))
                        .param("mileage", String.valueOf(addCarRequest.getMileage()))
                        .param("fuelConsumption", String.valueOf(addCarRequest.getFuelConsumption()))
                        .param("basePrice", String.valueOf(addCarRequest.getBasePrice()))
                        .param("deposit", String.valueOf(addCarRequest.getDeposit()))
                        .param("description", addCarRequest.getDescription())
                        .param("additionalFunction", addCarRequest.getAdditionalFunction())
                        .param("termOfUse", addCarRequest.getTermOfUse())
                        .param("automatic", String.valueOf(addCarRequest.isAutomatic()))
                        .param("gasoline", String.valueOf(addCarRequest.isGasoline()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("data.licensePlate").value("49F-123.45"));
    }

    @Test
    void addNewCar_Failed_BadRequest() throws Exception {
        // Given: Không truyền đầy đủ thông tin (Thiếu "brand" và "licensePlate")
        CarResponse carResponse = new CarResponse();
        carResponse.setLicensePlate("49F-123.45");
        carResponse.setBrand("Toyota");
        carResponse.setModel("Camry");
        carResponse.setColor("Black");
        carResponse.setNumberOfSeats(5);
        carResponse.setProductionYear(2020);
        carResponse.setMileage(15000);
        carResponse.setFuelConsumption(7.5f);
        carResponse.setBasePrice(50000);
        carResponse.setDeposit(500000);
        carResponse.setAddress("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng");
        carResponse.setDescription("This is a test");
        carResponse.setAdditionalFunction("Bluetooth");
        carResponse.setTermOfUse("No");
        carResponse.setAutomatic(true);
        carResponse.setGasoline(true);

        Mockito.when(carService.addNewCar(ArgumentMatchers.any())).thenReturn(carResponse);

        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "test.properties/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/car/addCar")
                        .file("registrationPaper", emptyFile.getBytes())
                        .file("certificateOfInspection", emptyFile.getBytes())
                        .file("insurance", emptyFile.getBytes())
                        .file("carImageFront", emptyFile.getBytes())
                        .file("carImageBack", emptyFile.getBytes())
                        .file("carImageLeft", emptyFile.getBytes())
                        .file("carImageRight", emptyFile.getBytes())
                        .param("model", addCarRequest.getModel())
                        .param("address", addCarRequest.getAddress())
                        .param("brand", addCarRequest.getBrand())
                        //  Không truyền "licensePlate"
                        .param("color", addCarRequest.getColor())
                        .param("numberOfSeats", String.valueOf(addCarRequest.getNumberOfSeats()))
                        .param("productionYear", String.valueOf(addCarRequest.getProductionYear()))
                        .param("mileage", String.valueOf(addCarRequest.getMileage()))
                        .param("fuelConsumption", String.valueOf(addCarRequest.getFuelConsumption()))
                        .param("basePrice", String.valueOf(addCarRequest.getBasePrice()))
                        .param("deposit", String.valueOf(addCarRequest.getDeposit()))
                        .param("description", addCarRequest.getDescription())
                        .param("additionalFunction", addCarRequest.getAdditionalFunction())
                        .param("termOfUse", addCarRequest.getTermOfUse())
                        .param("automatic", String.valueOf(addCarRequest.isAutomatic()))
                        .param("gasoline", String.valueOf(addCarRequest.isGasoline()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(2000));
    }
}