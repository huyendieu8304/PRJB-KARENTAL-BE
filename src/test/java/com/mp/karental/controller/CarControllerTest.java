package com.mp.karental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.karental.KarentalApplication;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.service.CarService;
import com.mp.karental.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


/**
 * Test for car controller
 *
 * @author QuangPM20
 *
 * @version 1.0
 */

@SpringBootTest(classes = KarentalApplication.class)
@ExtendWith(MockitoExtension.class)
@Slf4j
@AutoConfigureMockMvc
public class CarControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;
    @MockitoBean
    private JwtUtils jwtUtils;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private AccountRepository accountRepository;

    private AddCarRequest addCarRequest;
    private EditCarRequest editCarRequest;

    // Initialize the data before each test
    @BeforeEach
    void setUp() {
        // Disable security filter in test
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
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
        //editCarRequest

        editCarRequest = EditCarRequest.builder()
                .mileage(15000)
                .fuelConsumption(7.5f)
                .basePrice(50000)
                .deposit(500000)
                .address("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng")
                .description("This is a test after edit")
                .additionalFunction("Bluetooth")
                .termOfUse("Yes")
                .status("AVAILABLE")  // This can be either "AVAILABLE" or "STOPPED"
                .build();
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

        Mockito.when(carService.addNewCar(ArgumentMatchers.any())).thenReturn(carResponse);

        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "test.properties/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/car/car-owner/addCar")
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

        when(carService.addNewCar(ArgumentMatchers.any())).thenReturn(carResponse);

        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "test.properties/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/car/car-owner/addCar")
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
                        //  miss license plate
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

    @Test
    void editCar_Success() throws Exception {
        // Prepare expected response (after editing)
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
        carResponse.setDescription("This is a test after edit");
        carResponse.setAdditionalFunction("Bluetooth");
        carResponse.setTermOfUse("Yes");
        carResponse.setAutomatic(true);
        carResponse.setGasoline(true);

        // Mock the car service behavior
        when(carService.editCar(ArgumentMatchers.any(), anyString())).thenReturn(carResponse);

        // Mock the files for the multipart request (empty files just to satisfy the request)
        MockMultipartFile carImageFront = new MockMultipartFile("carImageFront", "", "image/jpeg", new byte[0]);
        MockMultipartFile carImageBack = new MockMultipartFile("carImageBack", "", "image/jpeg", new byte[0]);
        MockMultipartFile carImageLeft = new MockMultipartFile("carImageLeft", "", "image/jpeg", new byte[0]);
        MockMultipartFile carImageRight = new MockMultipartFile("carImageRight", "", "image/jpeg", new byte[0]);

        // Convert the EditCarRequest to JSON
        // Perform the test: Edit an existing car with the JWT token for authorization
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/car/car-owner/editCar/{id}", "carId123")  // Use multipart to simulate file uploads
                        .file(carImageFront)
                        .file(carImageBack)
                        .file(carImageLeft)
                        .file(carImageRight)
                        .param("mileage", String.valueOf(editCarRequest.getMileage()))
                        .param("fuelConsumption", String.valueOf(editCarRequest.getFuelConsumption()))
                        .param("basePrice", String.valueOf(editCarRequest.getBasePrice()))
                        .param("deposit", String.valueOf(editCarRequest.getDeposit()))
                        .param("address", editCarRequest.getAddress())
                        .param("description", editCarRequest.getDescription())
                        .param("additionalFunction", editCarRequest.getAdditionalFunction())
                        .param("termOfUse", editCarRequest.getTermOfUse())
                        .param("status", editCarRequest.getStatus())
                        .contentType(MediaType.MULTIPART_FORM_DATA))  // Set content type to multipart/form-data
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("data.licensePlate").value("49F-123.45"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.description").value("This is a test after edit"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.termOfUse").value("Yes"));
    }

    @Test
    void editCar_Failure_MissingCarImageFront() throws Exception {
        // Prepare expected response (after editing failure)
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
        carResponse.setDescription("This is a test after edit");
        carResponse.setAdditionalFunction("Bluetooth");
        carResponse.setTermOfUse("Yes");
        carResponse.setAutomatic(true);
        carResponse.setGasoline(true);

        // Mock the car service behavior to simulate failure
        when(carService.editCar(ArgumentMatchers.any(), anyString()))
                .thenThrow(new IllegalArgumentException("Car's front side image is required."));

        // Prepare EditCarRequest object (example)
        EditCarRequest editCarRequest = EditCarRequest.builder()
                .mileage(15000)
                .fuelConsumption(7.5f)
                .basePrice(50000)
                .deposit(500000)
                .address("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng")
                .description("This is a test after edit")
                .additionalFunction("Bluetooth")
                .termOfUse("Yes")
                .status("AVAILABLE")
                .build();

        // Mock the files for the multipart request (missing carImageFront to simulate failure)
        // Missing carImageFront file
        MockMultipartFile carImageBack = new MockMultipartFile("carImageBack", "", "image/jpeg", new byte[0]);
        MockMultipartFile carImageLeft = new MockMultipartFile("carImageLeft", "", "image/jpeg", new byte[0]);
        MockMultipartFile carImageRight = new MockMultipartFile("carImageRight", "", "image/jpeg", new byte[0]);

        // Perform the test: Edit an existing car with the JWT token for authorization
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/car/car-owner/editCar/{id}", "carId123")  // Explicitly use PUT method
                        .file(carImageBack)
                        .file(carImageLeft)
                        .file(carImageRight)
                        .param("mileage", String.valueOf(editCarRequest.getMileage()))
                        .param("fuelConsumption", String.valueOf(editCarRequest.getFuelConsumption()))
                        .param("basePrice", String.valueOf(editCarRequest.getBasePrice()))
                        .param("deposit", String.valueOf(editCarRequest.getDeposit()))
                        .param("address", editCarRequest.getAddress())
                        .param("description", editCarRequest.getDescription())
                        .param("additionalFunction", editCarRequest.getAdditionalFunction())
                        .param("termOfUse", editCarRequest.getTermOfUse())
                        .param("status", editCarRequest.getStatus())
                        .contentType(MediaType.MULTIPART_FORM_DATA))  // This sets the content type to multipart/form-data
                .andExpect(MockMvcResultMatchers.status().isBadRequest())  // Expect 400 Bad Request due to missing file
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(2000))  // Assuming the failure code
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Car's front side image is required."));
    }
}