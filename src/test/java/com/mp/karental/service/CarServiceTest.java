package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.*;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;
import java.util.Collections;

import java.time.LocalDateTime;
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
    private UserProfileRepository userProfileRepository;

    @Mock
    private BookingRepository bookingRepository;

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

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;



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
                .status(ECarStatus.STOPPED)
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
        String accountId = "user-123";

        // Mock SecurityUtil to return a fixed accountId
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);

        // Mock Account retrieval
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Prepare test request
        addCarRequest.setLicensePlate("49F-123.45");
        addCarRequest.setAutomatic(true);
        addCarRequest.setGasoline(true);
        addCarRequest.setAddress("Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211, Trần Duy Hưng");

        // Mock Car mapping
        Car mockCar = new Car();
        mockCar.setLicensePlate("49F-123.45");
        mockCar.setAccount(mockAccount);
        lenient().when(carMapper.toCar(any(AddCarRequest.class))).thenReturn(mockCar);

        // Mock repository save before file processing
        lenient().when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car car = invocation.getArgument(0);
            car.setId("car-123");
            return car;
        });

        // Mock CarResponse mapping
        CarResponse mockResponse = new CarResponse();
        mockResponse.setLicensePlate("49F-123.45");
        mockResponse.setId("car-123");
        lenient().when(carMapper.toCarResponse(any(Car.class))).thenReturn(mockResponse);

        // Execute service method
        CarResponse response = carService.addNewCar(addCarRequest);

        // Assertions
        assertNotNull(response, "Response should not be null");
        assertEquals("49F-123.45", response.getLicensePlate());
        assertEquals("car-123", response.getId());

        // Verify necessary interactions
        verify(carMapper).toCar(addCarRequest);
        verify(carRepository, times(2)).save(any(Car.class)); // Before and after file processing
        verify(carMapper).toCarResponse(any(Car.class));
    }

    @Test
    void addCar_duplicateLicensePlate_fail() {
        // Mock SecurityUtil returning a valid account ID
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock existing account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

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

        // Mock existing car in the database
        Car existingCar = new Car();
        existingCar.setId("car-123");
        existingCar.setLicensePlate("49F-123.45");
        existingCar.setAutomatic(false);
        existingCar.setGasoline(false);
        existingCar.setStatus(ECarStatus.NOT_VERIFIED);
        existingCar.setCityProvince("Tỉnh Hà Giang");
        existingCar.setDistrict("Thành phố Hà Giang");
        existingCar.setWard("Phường Quang Trung");
        existingCar.setHouseNumberStreet("211, Trần Duy Hưng");
        existingCar.setAccount(mockAccount);

        when(carRepository.findById("car-123")).thenReturn(Optional.of(existingCar));

        // Mock the save method properly
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car updatedCar = invocation.getArgument(0);
            updatedCar.setLicensePlate(existingCar.getLicensePlate());
            updatedCar.setAutomatic(existingCar.isAutomatic());
            updatedCar.setGasoline(existingCar.isGasoline());
            updatedCar.setStatus(ECarStatus.NOT_VERIFIED);
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
            response.setStatus(ECarStatus.NOT_VERIFIED.name().toUpperCase());
            response.setAddress(String.join(", ", updatedCar.getCityProvince(), updatedCar.getDistrict(), updatedCar.getWard(), updatedCar.getHouseNumberStreet()));
            response.setDescription(updatedCar.getDescription());
            return response;
        });

        // Execute the service method
        CarResponse response = carService.editCar(editCarRequest, "car-123");
        // Assertions
        assertNotNull(response, "Response should not be null");
        assertEquals("NOT_VERIFIED", response.getStatus());
        assertEquals("updated description", response.getDescription());
    }

    // Helper method to check valid status values
    private boolean isValidStatus(String status) {
        return List.of("AVAILABLE", "STOPPED").contains(status);
    }

    @Test
    void editCar_shouldThrowException_whenCarNotFound() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        when(carRepository.findById("car-999")).thenReturn(Optional.empty());

        EditCarRequest editCarRequest = new EditCarRequest();
        editCarRequest.setStatus("AVAILABLE");

        assertThrows(AppException.class, () -> carService.editCar(editCarRequest, "car-999"));
    }

    @Test
    void editCar_unauthorizedUser_throwsException() {
        // Step 1: Mock SecurityUtil to return a different account ID (not the owner)
        String loggedInAccountId = "user-456"; // Different user
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(loggedInAccountId);

        // Step 2: Mock existing account (for logged-in user)
        Account loggedInAccount = new Account();
        loggedInAccount.setId(loggedInAccountId);

        // Step 3: Mock car owned by a different user
        String ownerAccountId = "user-123"; // Original owner of the car
        Account ownerAccount = new Account();
        ownerAccount.setId(ownerAccountId);

        Car existingCar = new Car();
        existingCar.setId("car-123");
        existingCar.setAccount(ownerAccount); // Car belongs to another user

        when(carRepository.findById("car-123")).thenReturn(Optional.of(existingCar));

        // Step 4: Execute the service method and expect an exception
        AppException exception = assertThrows(AppException.class,
                () -> carService.editCar(editCarRequest, "car-123"));

        // Step 5: Assert that the exception is due to unauthorized access
        assertEquals(ErrorCode.FORBIDDEN_CAR_ACCESS, exception.getErrorCode());
    }


    @Test
    void getCarById_ShouldReturnCarResponse_WhenCarExists() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        Car car;
        CarResponse carResponse;
        car = new Car();
        car.setId("123");
        car.setCityProvince("Hanoi");
        car.setDistrict("Hoan Kiem");
        car.setWard("Ly Thai To");
        car.setHouseNumberStreet("24 Trang Tien");
        car.setAccount(mockAccount);

        carResponse = new CarResponse();
        carResponse.setId("123");
        carResponse.setAddress("Hanoi, Hoan Kiem, Ly Thai To, 24 Trang Tien");
        when(carRepository.findById("123")).thenReturn(Optional.of(car));
        when(carMapper.toCarResponse(car)).thenReturn(carResponse);

        CarResponse result = carService.getCarById("123");

        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("Hanoi, Hoan Kiem, Ly Thai To, 24 Trang Tien", result.getAddress());

        verify(carRepository, times(1)).findById("123");
        verify(carMapper, times(1)).toCarResponse(car);
    }

    @Test
    void getCarById_ShouldThrowException_WhenCarNotFound() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(carRepository.findById("999")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> carService.getCarById("999"));

        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());

        verify(carRepository, times(1)).findById("999");
        verify(carMapper, never()).toCarResponse(any());
    }

    @Test
    void getCarById_UnauthorizedAccess_ShouldThrowException() {
        // Simulate logged-in user account
        String loggedInAccountId = "account123";
        String carOwnerId = "account456"; // Different owner

        // Mock SecurityUtil to return the logged-in user's ID
        when(SecurityUtil.getCurrentAccountId()).thenReturn(loggedInAccountId);

        // Mock account retrieval (valid account)
        Account loggedInAccount = new Account();
        loggedInAccount.setId(loggedInAccountId);

        // Mock car retrieval (belongs to another account)
        Account carOwner = new Account();
        carOwner.setId(carOwnerId);
        Car car = new Car();
        car.setId("car123");
        car.setAccount(carOwner);

        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        // Expect unauthorized access exception
        AppException exception = assertThrows(AppException.class, () -> carService.getCarById("car123"));
        assertEquals(ErrorCode.FORBIDDEN_CAR_ACCESS, exception.getErrorCode());
    }

    @Test
    void testGetCarDetail_WhenCarExistsAndVerified_ShouldReturnDetails() {
        String accountId = "user-123";
        String carId = "car-1";

        Mockito.when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);
        car.setCityProvince("Hanoi");
        car.setDistrict("Ba Dinh");
        car.setWard("Doi Can");
        car.setHouseNumberStreet("123");

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(eq(carId), eq(accountId), anyList()))
                .thenReturn(true);
        when(carMapper.toCarDetailResponse(any(Car.class), anyBoolean())).thenReturn(new CarDetailResponse());

        CarDetailRequest request = new CarDetailRequest(carId, LocalDateTime.now(), LocalDateTime.now().plusDays(2));
        CarDetailResponse response = carService.getCarDetail(request);

        assertNotNull(response);
        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).toCarDetailResponse(any(Car.class), anyBoolean());
    }


    @Test
    void testIsCarAvailable_WhenNoBookings_ShouldReturnTrue() {
        String carId = "car-1";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(2);

        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(eq(carId), any(), any()))
                .thenReturn(Collections.emptyList());

        boolean result = carService.isCarAvailable(carId, pickUpTime, dropOffTime);

        assertTrue(result);
        verify(bookingRepository, times(1)).findActiveBookingsByCarIdAndTimeRange(eq(carId), any(), any());
    }

    @Test
    void searchCars_ReturnsEmptyList() {
        // Mock request
        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));

        // Mock danh sách rỗng
        Page<Car> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(carRepository.findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable)).thenReturn(emptyPage);

        // Call service
        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");

        // Kiểm tra kết quả
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());

        // Kiểm tra mock đã được gọi
        verify(carRepository, times(1)).findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable);
    }


    @Test
    void getCarsByUserId_ShouldUseDefaultSize_WhenSizeInvalid() {
        // ✅ Kiểm tra size < 0 hoặc size > 100
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));

        when(carRepository.findByAccountId("user123", pageable)).thenReturn(Page.empty());

        Page<CarThumbnailResponse> result = carService.getCarsByUserId(0, -5, "productionYear,desc");

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getCarDetail_ShouldThrowException_WhenPickUpAfterDropOff() {
        // ✅ Test pickUpTime > dropOffTime
        CarDetailRequest request = new CarDetailRequest();
        request.setPickUpTime(LocalDateTime.now().plusDays(2));
        request.setDropOffTime(LocalDateTime.now().plusDays(1));

        assertThrows(AppException.class, () -> carService.getCarDetail(request));
    }

    @Test
    void isCarAvailable_ShouldReturnFalse_WhenCarHasActiveBookings() {
        // ✅ Test có booking không CANCELLED hoặc PENDING_DEPOSIT
        Car car = new Car();
        car.setId("car123");
        Booking booking = mock(Booking.class);
        Booking activeBooking = new Booking();
        activeBooking.setBookingNumber("booking1");
        activeBooking.setCar(car);
        activeBooking.setStatus(EBookingStatus.CONFIRMED); // Trạng thái khiến xe không available
        activeBooking.setPickUpTime(LocalDateTime.now().plusDays(1));
        activeBooking.setDropOffTime(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(eq("car123"), any(), any()))
                .thenReturn(List.of(activeBooking)); // Có booking đang active => EXPECTED: false
        boolean result = carService.isCarAvailable("car123", LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertFalse(result);
    }

    @Test
    void isCarBooked_ShouldReturnTrue_WhenBookingExists() {
        // ✅ Test có booking
        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(any(), any(), any()))
                .thenReturn(true);

        boolean result = carService.isCarBooked("car123", "user123");

        assertTrue(result);
    }

    @Test
    void searchCars_ShouldReturnEmpty_WhenNoVerifiedCars() {
        // ✅ Test không có xe VERIFIED
        when(carRepository.findVerifiedCarsByAddress(any(), any(), any())).thenReturn(Page.empty());

        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testSearchCars_ReturnsAvailableCars() {
        // Arrange
        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Car car = new Car();
        car.setId("1");
        car.setWard("Ba Dinh");
        car.setCityProvince("Hanoi");
        car.setCarImageFront("front.jpg");
        car.setCarImageBack("back.jpg");
        car.setCarImageLeft("left.jpg");
        car.setCarImageRight("right.jpg");

        List<Car> carList = List.of(car);
        Page<Car> carPage = new PageImpl<>(carList);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));

        when(carRepository.findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable))
                .thenReturn(carPage);
        when(bookingRepository.countCompletedBookingsByCar(car.getId()))
                .thenReturn(5L);
        when(carMapper.toSearchCar(any(), anyLong()))
                .thenReturn(new CarThumbnailResponse());
        when(fileService.getFileUrl(anyString()))
                .thenReturn("http://example.com/image.jpg");

        // Act
        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(carRepository).findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable);
        verify(carMapper).toSearchCar(any(), anyLong());
    }

    @Test
    void testSearchCars_NoVerifiedCars_ReturnsEmptyPage() {
        // Arrange
        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));

        when(carRepository.findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable))
                .thenReturn(Page.empty());

        // Act
        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(carRepository).findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable);
    }

    @Test
    void testSearchCars_InvalidSize_DefaultTo10() {
        // Arrange
        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));

        when(carRepository.findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable))
                .thenReturn(Page.empty());

        // Act
        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, -5, "productionYear,desc");

        // Assert
        assertEquals(0, result.getTotalElements()); // No cars
        verify(carRepository).findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable);
    }

    @Test
    void testSearchCars_InvalidPage_DefaultTo0() {
        // Arrange
        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));

        when(carRepository.findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable))
                .thenReturn(Page.empty());

        // Act
        Page<CarThumbnailResponse> result = carService.searchCars(request, -3, 10, "productionYear,desc");

        // Assert
        assertEquals(0, result.getTotalElements()); // No cars
        verify(carRepository).findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable);
    }

    @Test
    void testGetCarDetail_CarNotFound_ThrowsException() {
        // Arrange
        CarDetailRequest request = new CarDetailRequest();
        request.setCarId("99");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        when(carRepository.findById("99")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () -> carService.getCarDetail(request));
    }

    @Test
    void testGetCarDetail_CarNotVerified_ThrowsException() {
        // Arrange
        CarDetailRequest request = new CarDetailRequest();
        request.setCarId("3");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Car car = new Car();
        car.setId("3");
        car.setStatus(ECarStatus.NOT_VERIFIED); // Not VERIFIED

        when(carRepository.findById("3")).thenReturn(Optional.of(car));

        // Act & Assert
        assertThrows(AppException.class, () -> carService.getCarDetail(request));
    }

    @Test
    void testGetCarDetail_InvalidDateRange_ThrowsException() {
        // Arrange
        CarDetailRequest request = new CarDetailRequest();
        request.setCarId("4");
        request.setPickUpTime(LocalDateTime.now().plusDays(3));
        request.setDropOffTime(LocalDateTime.now().plusDays(2)); // Invalid: Pick-up after drop-off

        // Act & Assert
        assertThrows(AppException.class, () -> carService.getCarDetail(request));
    }

    @Test
    void getCarDetail_UnbookedCar_HidesSensitiveData() {
        // Arrange
        String accountId = "user123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        CarDetailRequest request = new CarDetailRequest();
        request.setCarId("1");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(3));

        Car car = new Car();
        car.setId("1");
        car.setDistrict("District 1");
        car.setCityProvince("Ho Chi Minh City");
        car.setStatus(ECarStatus.VERIFIED);

        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(bookingRepository.countCompletedBookingsByCar("1")).thenReturn(5L);
        when(carMapper.toCarDetailResponse(any(), anyBoolean())).thenReturn(new CarDetailResponse());
        when(carService.isCarBooked("1", accountId)).thenReturn(false);

        // Act
        CarDetailResponse response = carService.getCarDetail(request);

        // Assert
        assertNotNull(response);
        assertNull(response.getCertificateOfInspectionUrl());
        assertNull(response.getInsuranceUrl());
        assertNull(response.getRegistrationPaperUrl());
        assertEquals("District 1, Ho Chi Minh City (Full address will be available after you've paid the deposit to rent).", response.getAddress());
    }

}