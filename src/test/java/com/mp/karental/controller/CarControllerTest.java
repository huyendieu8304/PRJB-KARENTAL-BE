package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.car.AddCarRequest;
import com.mp.karental.dto.request.car.CarDetailRequest;
import com.mp.karental.dto.response.car.CarDetailResponse;
import com.mp.karental.dto.request.car.EditCarRequest;
import com.mp.karental.dto.response.car.CarResponse;
import com.mp.karental.dto.response.car.CarThumbnailResponse;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.exception.AppException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private CarDetailResponse carDetailResponse;
    @MockitoBean
    private CarThumbnailResponse carThumbnailResponse;

    private AddCarRequest addCarRequest;
    private EditCarRequest editCarRequest;
    private CarDetailRequest carDetailRequest;

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
                .status(ECarStatus.STOPPED)  // This can be either "AVAILABLE" or "STOPPED"
                .build();

        carDetailRequest = CarDetailRequest.builder()
                .carId("123")
                .pickUpTime(LocalDateTime.of(2025, 3, 12, 6, 0)) // 12/03/2025 06:00 AM
                .dropOffTime(LocalDateTime.of(2025, 3, 15, 22, 0)) // 15/03/2025 10:00 PM
                .build();
    }

    @Test
    void addNewCar_Success() throws Exception {
        // Prepare expected response
        CarResponse carResponse = new CarResponse();
        carResponse.setLicensePlate("11F-111.11");
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

        mockMvc.perform(multipart("/car/car-owner/add-car")
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
                        .param("licensePlate", "11F-111.11")
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
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("data.licensePlate").value("11F-111.11"));
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

        mockMvc.perform(multipart("/car/car-owner/add-car")
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
                .andExpect(status().isBadRequest())
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
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/car/car-owner/edit-car/{id}", "carId123")  // Use multipart to simulate file uploads
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
                        .param("status", String.valueOf(editCarRequest.getStatus()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))  // Set content type to multipart/form-data
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("data.licensePlate").value("49F-123.45"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.description").value("This is a test after edit"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.termOfUse").value("Yes"));
    }

    @Test
    void getCarById_ShouldReturnCarResponse_WhenCarExists() throws Exception {
        CarResponse carResponse = new CarResponse();
        carResponse.setId("123");
        carResponse.setAddress("Hanoi, Hoan Kiem, Ly Thai To, 24 Trang Tien");

        when(carService.getCarById("123")).thenReturn(carResponse);

        mockMvc.perform(get("/car/car-owner/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("data.id").value("123"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.address").value("Hanoi, Hoan Kiem, Ly Thai To, 24 Trang Tien"));
    }
    @Test
    void getCarById_ShouldReturn404_WhenCarNotFound() throws Exception {
        when(carService.getCarById("999")).thenThrow(new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        mockMvc.perform(get("/car/car-owner/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "owner", roles = {"OWNER"})
    void getMyCars_Success() throws Exception {
        // Mock car list
        CarThumbnailResponse car1 = CarThumbnailResponse.builder()
                .id("1")
                .brand("Toyota")
                .model("Camry")
                .productionYear(2020)
                .basePrice(50000)
                .address("Hà Nội")
                .build();

        CarThumbnailResponse car2 = CarThumbnailResponse.builder()
                .id("2")
                .brand("Honda")
                .model("Civic")
                .productionYear(2019)
                .basePrice(45000)
                .address("TP.HCM")
                .build();


        List<CarThumbnailResponse> cars = List.of(car1, car2);
        Page<CarThumbnailResponse> carPage = new PageImpl<>(cars);

        // Mock service behavior
        Mockito.when(carService.getCarsByUserId(0, 10, "productionYear,DESC")).thenReturn(carPage);

        mockMvc.perform(get("/car/car-owner/my-cars")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "productionYear,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].brand").value("Toyota"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[1].brand").value("Honda"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"OWNER"})
    void getMyCars_EmptyList() throws Exception {
        Page<CarThumbnailResponse> emptyPage = Page.empty();
        Mockito.when(carService.getCarsByUserId(0, 10, "productionYear,DESC")).thenReturn(emptyPage);

        mockMvc.perform(get("/car/car-owner/my-cars")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "productionYear,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty());
    }


    @Test
    void getCarDetail_Success() throws Exception {
        CarDetailResponse carDetail = CarDetailResponse.builder()
                .licensePlate("49F-123.45")
                .brand("Toyota")
                .model("Camry")
                .status("Available")
                .color("Black")
                .numberOfSeats(5)
                .productionYear(2020)
                .mileage(15000)
                .fuelConsumption(7.5f)
                .basePrice(50000)
                .deposit(500000)
                .address("Hà Nội")
                .description("Test car")
                .additionalFunction("Bluetooth")
                .termOfUse("No")
                .isAutomatic(true)
                .isGasoline(true)
                .build();

        Mockito.when(carService.getCarDetail(Mockito.any())).thenReturn(carDetail);

        mockMvc.perform(get("/car/customer/car-detail")
                        .param("carId", "123")
                        .param("pickUpTime", "2025-03-12T06:00:00")
                        .param("dropOffTime", "2025-03-15T22:00:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.licensePlate").value("49F-123.45"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.brand").value("Toyota"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.model").value("Camry"));
    }



    @Test
    void getCarDetail_NoError() throws Exception {
        String carId = "123";
        CarDetailResponse carDetail = CarDetailResponse.builder()
                .licensePlate("51A-678.90")
                .brand("Honda")
                .model("Civic")
                .status("Available")
                .color("White")
                .numberOfSeats(5)
                .productionYear(2021)
                .mileage(10000)
                .fuelConsumption(6.5f)
                .basePrice(60000)
                .deposit(600000)
                .address("Hồ Chí Minh")
                .description("Test car without error")
                .additionalFunction("Sunroof")
                .termOfUse("Yes")
                .isAutomatic(true)
                .isGasoline(true)
                .build();

        Mockito.when(carService.getCarDetail(Mockito.any())).thenReturn(carDetail);

        mockMvc.perform(get("/car/customer/car-detail")
                        .param("carId", "123")
                        .param("pickUpTime", "2025-03-12T06:00:00")
                        .param("dropOffTime", "2025-03-15T22:00:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.licensePlate").value("51A-678.90"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.brand").value("Honda"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.model").value("Civic"));
    }

    @Test
    void searchCars_Success() throws Exception {
        // Arrange: Mock data
        LocalDateTime pickUpTime = LocalDateTime.of(2025, 3, 12, 6, 0);
        LocalDateTime dropOffTime = LocalDateTime.of(2025, 3, 15, 22, 0);

        CarThumbnailResponse car1 = CarThumbnailResponse.builder()
                .id("1")
                .brand("Toyota")
                .model("Camry")
                .productionYear(2020)
                .status("Available")
                .mileage(15000)
                .basePrice(50000)
                .address("Hà Nội")
                .carImageFront("image_front_url")
                .carImageRight("image_right_url")
                .carImageLeft("image_left_url")
                .carImageBack("image_back_url")
                .noOfRides(10)
                .build();

        Page<CarThumbnailResponse> carPage = new PageImpl<>(Collections.singletonList(car1));

        Mockito.when(carService.searchCars(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(carPage);

        // Act & Assert
        mockMvc.perform(get("/car/customer/search-car")
                        .param("address", "Hà Nội")
                        .param("pickUpTime", "2025-03-12T06:00:00")
                        .param("dropOffTime", "2025-03-15T22:00:00")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "productionYear,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].brand").value("Toyota"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].model").value("Camry"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].productionYear").value(2020))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].status").value("Available"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].mileage").value(15000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].basePrice").value(50000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].address").value("Hà Nội"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].carImageFront").value("image_front_url"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].carImageRight").value("image_right_url"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].carImageLeft").value("image_left_url"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].carImageBack").value("image_back_url"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].noOfRides").value(10));
    }

    @Test
    void searchCars_Failed_InvalidDateFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/car/customer/search-car")
                        .param("address", "Hà Nội")
                        .param("pickUpTime", "invalid-date-format")  
                        .param("dropOffTime", "2025-03-15T22:00:00")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "productionYear,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()) 
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(2029));
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void getCarListForOperator_Success() throws Exception {
        // Mock car list
        CarThumbnailResponse car1 = CarThumbnailResponse.builder()
                .id("1")
                .brand("Toyota")
                .model("Camry")
                .productionYear(2020)
                .status("NOT_VERIFIED")
                .mileage(15000)
                .basePrice(50000)
                .address("Hà Nội")
                .updatedAt(LocalDateTime.now())
                .build();

        CarThumbnailResponse car2 = CarThumbnailResponse.builder()
                .id("2")
                .brand("Honda")
                .model("Civic")
                .productionYear(2019)
                .status("VERIFIED")
                .mileage(20000)
                .basePrice(45000)
                .address("TP.HCM")
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        List<CarThumbnailResponse> cars = List.of(car1, car2);
        Page<CarThumbnailResponse> carPage = new PageImpl<>(cars);

        // Mock service behavior
        Mockito.when(carService.getAllCarsForOperator(0, 10, "updatedAt,desc", null)).thenReturn(carPage);

        mockMvc.perform(get("/car/operator/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "updatedAt,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].brand").value("Toyota"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[1].brand").value("Honda"));
    }

    @Test
    @WithMockUser(username = "operator", roles = {"OPERATOR"})
    void getCarListForOperator_EmptyList() throws Exception {
        Page<CarThumbnailResponse> emptyPage = Page.empty();
        Mockito.when(carService.getAllCarsForOperator(0, 10, "updatedAt,desc", null)).thenReturn(emptyPage);

        mockMvc.perform(get("/car/operator/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "updatedAt,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty());
    }

}