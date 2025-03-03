package com.mp.karental.service;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditPasswordRequest;
import com.mp.karental.dto.request.EditProfileRequest;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.mapper.UserMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.SecurityUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private FileService fileService;
    @Mock
    private AddCarRequest addCarRequest;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;


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

        // Mock repository
        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(carPage);

        // Mock fileService
        when(fileService.getFileUrl(anyString())).thenReturn("https://example.com/image.jpg");

        // Mock carMapper để tránh null
        CarThumbnailResponse carThumbnailResponse = new CarThumbnailResponse();
        when(carMapper.toCarThumbnailResponse(any(Car.class))).thenReturn(carThumbnailResponse);

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
        AddCarRequest addCarRequest = new AddCarRequest();
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
    @WithMockUser
    void addCar_userNotFound() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("not_exist");

            when(accountRepository.findById("not_exist")).thenReturn(Optional.empty()); // Không tìm thấy tài khoản

            // Khi tài khoản không tồn tại, mong đợi ngoại lệ xảy ra
            Assertions.assertThrows(AppException.class, () -> carService.addNewCar(addCarRequest));

    }


    @Test
    void testGetCarDetail_WhenCarExistsAndNotBooked_ShouldReturnCarResponseWithHiddenAddress() {
        String carId = "car-123";
        Car car = new Car();
        car.setId(carId);
        car.setStatus("AVAILABLE");
        car.setHouseNumberStreet("123 Main St");
        car.setWard("Ward 1");
        car.setDistrict("District A");
        car.setCityProvince("City X");

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        CarDetailResponse mockResponse = new CarDetailResponse();
        mockResponse.setAddress("Note: Full address will be available after you've paid the deposit to rent.");
        mockResponse.setNoOfRides(8);
        when(carMapper.toCarDetailResponse(any(Car.class), eq(false))).thenReturn(mockResponse);

        CarDetailResponse response = carService.getCarDetail(carId);

        assertNotNull(response);
    }

    @Test
    void testGetCarDetail_WhenCarExistsAndBooked_ShouldReturnCarResponseWithFullAddress() {
        // Given
        String carId = "car-456";
        String accountId = "user-123"; // Giả lập tài khoản hiện tại
        Car car = new Car();
        car.setId(carId);
        car.setStatus("BOOKED");
        car.setHouseNumberStreet("123 Main St");
        car.setWard("Ward 1");
        car.setDistrict("District A");
        car.setCityProvince("City X");
        car.setRegistrationPaperUri("s3://documents/registration.pdf");
        car.setCertificateOfInspectionUri("s3://documents/inspection.pdf");
        car.setInsuranceUri("s3://documents/insurance.pdf");

        // Mock repository và service
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(bookingRepository.isCarBookedByAccount(carId, accountId)).thenReturn(true);
        when(bookingRepository.countCompletedBookingsByCar(carId)).thenReturn(8L);

        when(fileService.getFileUrl("s3://documents/registration.pdf")).thenReturn("https://cdn.example.com/registration.pdf");
        when(fileService.getFileUrl("s3://documents/inspection.pdf")).thenReturn("https://cdn.example.com/inspection.pdf");
        when(fileService.getFileUrl("s3://documents/insurance.pdf")).thenReturn("https://cdn.example.com/insurance.pdf");

        when(carMapper.toCarDetailResponse(car, true)).thenReturn(new CarDetailResponse());

        // When
        CarDetailResponse response = carService.getCarDetail(carId);

        // Then
        assertNotNull(response);
        assertEquals("123 Main St, Ward 1, District A, City X", response.getAddress());
        assertEquals("https://cdn.example.com/registration.pdf", response.getRegistrationPaperUrl());
        assertEquals("https://cdn.example.com/inspection.pdf", response.getCertificateOfInspectionUrl());
        assertEquals("https://cdn.example.com/insurance.pdf", response.getInsuranceUrl());
        assertEquals(8, response.getNoOfRides());
        assertTrue(response.isRegistrationPaperIsVerified());
        assertTrue(response.isCertificateOfInspectionIsVerified());
        assertTrue(response.isInsuranceIsVerified());
    }


    @Test
    void testGetCarDetail_WhenCarDoesNotExist_ShouldThrowException() {
        String carId = "car-999";
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> carService.getCarDetail(carId));

        assertEquals(ErrorCode.CAR_NOT_FOUND, exception.getErrorCode());
    }

}



