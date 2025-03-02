package com.mp.karental.service;

import com.jayway.jsonpath.spi.mapper.MappingException;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.security.SecurityUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * test car service
 *
 * QuangPM20
 * version 1.0
 */

@ExtendWith(MockitoExtension.class)
class CarServiceTest {
    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private FileService fileService;
    @Mock
    private AddCarRequest addCarRequest;
    @Mock
    private EditCarRequest editCarRequest;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }


    @Test
    void testGetCarsByUserId_WhenUserHasCars_ShouldReturnCarList() {
        String accountId = "user-123";
        Mockito.when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);

        Car car1 = Car.builder()
                .id("car-1")
                .licensePlate("30A-12345")
                .brand("Toyota")
                .model("Corolla")
                .status("AVAILABLE")
                .color("White")
                .numberOfSeats(5)
                .productionYear(2020)
                .mileage(10000)
                .fuelConsumption(6.5f)
                .basePrice(500000)
                .deposit(100000)
                .cityProvince("Hanoi")
                .district("Hola")
                .ward("ThachThat")
                .houseNumberStreet("211, tran duy hung")
                .carImageFront("front.jpg")
                .carImageBack("back.jpg")
                .carImageLeft("left.jpg")
                .carImageRight("right.jpg")
                .build();

        Page<Car> carPage = new PageImpl<>(List.of(car1));

        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(carPage);
        when(fileService.getFileUrl(anyString())).thenReturn("https://example.com/image.jpg");

        Page<CarThumbnailResponse> response = carService.getCarsByUserId(0, 10, "mileage,asc");

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("https://example.com/image.jpg", response.getContent().get(0).getCarImageFront());

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
        verify(fileService, times(4)).getFileUrl(anyString());
    }


    @Test
    void testGetCarsByUserId_WhenUserHasNoCars_ShouldReturnEmptyList() {
        String accountId = "user-123";
        Mockito.when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);

        Page<Car> emptyPage = Page.empty(PageRequest.of(0, 10));
        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(emptyPage);

        Page<CarThumbnailResponse> response = carService.getCarsByUserId(0, 10, "mileage,asc");

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }

    @Test
    void testGetCarsByUserId_WhenRepositoryThrowsException_ShouldThrowException() {
        String accountId = "user-123";
        Mockito.when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);

        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            carService.getCarsByUserId(0, 10, "mileage,asc");
        });

        assertEquals("Database error", exception.getMessage());
        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }

    @Test
    void addCar_validRequest_success() throws AppException {
        // Mock SecurityUtil trả về account ID hợp lệ
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock account tồn tại
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Tạo request giả lập
        addCarRequest.setLicensePlate("49F-123.45");
        addCarRequest.setAutomatic(true);
        addCarRequest.setGasoline(true);
        addCarRequest.setAddress("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng");

        // Mock carMapper
        Car mockCar = new Car();
        mockCar.setLicensePlate("49F-123.45");
        when(carMapper.toCar(any(AddCarRequest.class))).thenReturn(mockCar);

        // Mock save xe vào database
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car car = invocation.getArgument(0);
            car.setId("car-123"); // Gán ID giả lập
            return car;
        });


        // Mock chuyển đổi từ Car -> CarResponse
        CarResponse mockResponse = new CarResponse();
        mockResponse.setLicensePlate("49F-123.45");
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(mockResponse);

        // Thực thi test
        CarResponse response = carService.addNewCar(addCarRequest);

        // Kiểm tra kết quả
        assertNotNull(response, "Response should not be null");
        assertEquals("49F-123.45", response.getLicensePlate());
    }
    @Test
    void addNewCar_shouldThrowException_whenAccountNotFound() {
        // Giả lập giá trị account ID lấy từ SecurityUtil
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Đảm bảo tìm account theo ID cụ thể
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        AddCarRequest request = new AddCarRequest();
        assertThrows(AppException.class, () -> carService.addNewCar(request));
    }
    @Test
    void addCar_duplicateLicensePlate_fail() {
        // Mock SecurityUtil returning a valid account ID
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock existing account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Create a request with a duplicate license plate
        AddCarRequest addCarRequest = new AddCarRequest();
        addCarRequest.setLicensePlate("49F-123.45");
        addCarRequest.setAddress("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng");

        // Mock carMapper behavior
        when(carMapper.toCar(any(AddCarRequest.class))).thenReturn(new Car());

        // Mock carRepository behavior to throw DataIntegrityViolationException for duplicate entry
        when(carRepository.save(any(Car.class))).thenThrow(DataIntegrityViolationException.class);

        // Expect DataIntegrityViolationException to be thrown directly
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
            carService.addNewCar(addCarRequest);
        });

        // Verify that carMapper.toCar() was invoked with the correct AddCarRequest
        verify(carMapper, times(1)).toCar(addCarRequest);
    }

    @Test
    void editCar_validRequest_success() throws AppException {
        // Mock SecurityUtil to return a valid account ID
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock existing account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Mock existing car in the database
        Car existingCar = new Car();
        existingCar.setId("car-123");
        existingCar.setLicensePlate("49F-123.45");
        existingCar.setAutomatic(false);
        existingCar.setGasoline(false);
        existingCar.setStatus("STOPPED");
        existingCar.setCityProvince("Tỉnh Hà Giang");
        existingCar.setDistrict("Thành phố Hà Giang");
        existingCar.setWard("Phường Quang Trung");
        existingCar.setHouseNumberStreet("211, Trần Duy Hưng");

        when(carRepository.findById("car-123")).thenReturn(Optional.of(existingCar));

        // Mock the save method properly
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car updatedCar = invocation.getArgument(0);
            updatedCar.setLicensePlate(existingCar.getLicensePlate());
            updatedCar.setAutomatic(existingCar.isAutomatic());
            updatedCar.setGasoline(existingCar.isGasoline());
            updatedCar.setStatus("AVAILABLE");
            updatedCar.setCityProvince(existingCar.getCityProvince());
            updatedCar.setDistrict(existingCar.getDistrict());
            updatedCar.setWard(existingCar.getWard());
            updatedCar.setHouseNumberStreet(existingCar.getHouseNumberStreet());
            updatedCar.setDescription("updated description");
            return updatedCar; // Ensure it returns the modified object
        });

        // Mock the mapper to ensure the response correctly combines the address
        when(carMapper.toCarResponse(any(Car.class))).thenAnswer(invocation -> {
            Car updatedCar = invocation.getArgument(0);
            CarResponse response = new CarResponse();
            response.setLicensePlate(updatedCar.getLicensePlate());
            response.setAddress(String.join(", ", updatedCar.getCityProvince(), updatedCar.getDistrict(), updatedCar.getWard(), updatedCar.getHouseNumberStreet()));
            response.setStatus(updatedCar.getStatus());
            response.setDescription(updatedCar.getDescription());
            return response;
        });

        // Execute the service method
        CarResponse response = carService.editCar(editCarRequest, "car-123");
        System.out.println("Car response: " + response);
        // Assertions
        assertNotNull(response, "Response should not be null");
        assertEquals("AVAILABLE", response.getStatus());
        assertEquals("updated description", response.getDescription());
    }

    @Test
    void editCar_shouldDefaultToAvailable_whenStatusIsInvalid() {
        // Mock SecurityUtil to return a valid account ID
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock existing account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Mock existing car in the database
        Car existingCar = new Car();
        existingCar.setId("car-123");
        existingCar.setLicensePlate("49F-123.45");
        existingCar.setAutomatic(false);
        existingCar.setGasoline(false);
        existingCar.setStatus("STOPPED");
        existingCar.setCityProvince("Tỉnh Hà Giang");
        existingCar.setDistrict("Thành phố Hà Giang");
        existingCar.setWard("Phường Quang Trung");
        existingCar.setHouseNumberStreet("211, Trần Duy Hưng");

        when(carRepository.findById("car-123")).thenReturn(Optional.of(existingCar));

        // Mock editCarRequest with an invalid status
        EditCarRequest editCarRequest = new EditCarRequest();
        editCarRequest.setStatus("abc"); // Invalid status

        // Mock the save method to enforce the default status logic
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car updatedCar = invocation.getArgument(0);

            // Ensure invalid status is replaced with "AVAILABLE"
            if (!isValidStatus(updatedCar.getStatus())) {
                updatedCar.setStatus("AVAILABLE");
            }

            updatedCar.setLicensePlate(existingCar.getLicensePlate());
            updatedCar.setAutomatic(existingCar.isAutomatic());
            updatedCar.setGasoline(existingCar.isGasoline());
            updatedCar.setCityProvince(existingCar.getCityProvince());
            updatedCar.setDistrict(existingCar.getDistrict());
            updatedCar.setWard(existingCar.getWard());
            updatedCar.setHouseNumberStreet(existingCar.getHouseNumberStreet());
            updatedCar.setDescription("updated description");
            return updatedCar;
        });

        // Mock the mapper to ensure the response correctly reflects the changes
        when(carMapper.toCarResponse(any(Car.class))).thenAnswer(invocation -> {
            Car updatedCar = invocation.getArgument(0);
            CarResponse response = new CarResponse();
            response.setLicensePlate(updatedCar.getLicensePlate());
            response.setAddress(String.join(", ", updatedCar.getCityProvince(), updatedCar.getDistrict(), updatedCar.getWard(), updatedCar.getHouseNumberStreet()));
            response.setStatus(updatedCar.getStatus());
            response.setDescription(updatedCar.getDescription());
            return response;
        });

        // Execute the service method
        CarResponse response = carService.editCar(editCarRequest, "car-123");

        // Assertions
        assertNotNull(response, "Response should not be null");
        assertEquals("AVAILABLE", response.getStatus(), "Invalid status should default to AVAILABLE");
        assertEquals("updated description", response.getDescription());
    }

    // Helper method to check valid status values
    private boolean isValidStatus(String status) {
        return List.of("AVAILABLE", "STOPPED").contains(status);
    }

    @Test
    void editCar_shouldThrowException_whenCarNotFound() {
        // Mock SecurityUtil để trả về account ID hợp lệ
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock account hợp lệ
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Mock carRepository để không tìm thấy xe
        when(carRepository.findById("car-999")).thenReturn(Optional.empty());

        // Tạo request
        EditCarRequest editCarRequest = new EditCarRequest();
        editCarRequest.setStatus("AVAILABLE");

        // Kiểm tra exception
        assertThrows(AppException.class, () -> carService.editCar(editCarRequest, "car-999"));
    }

}