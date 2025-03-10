package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.request.EditPasswordRequest;
import com.mp.karental.dto.request.EditProfileRequest;
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
        existingCar.setStatus(ECarStatus.STOPPED);
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
            updatedCar.setStatus(ECarStatus.STOPPED);
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
            response.setDescription(updatedCar.getDescription());
            return response;
        });

        // Execute the service method
        CarResponse response = carService.editCar(editCarRequest, "car-123");
        // Assertions
        assertNotNull(response, "Response should not be null");
        assertEquals("updated description", response.getDescription());
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
    void testGetCarDetail_WhenCarExistsAndNotBooked_ShouldReturnCarResponseWithHiddenAddress() {
        String carId = "car-123";
        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.STOPPED);
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
        Account account = new Account();
        String carId = "car-456";
        String accountId = "user-123"; // Giả lập tài khoản hiện tại
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.STOPPED);
        car.setHouseNumberStreet("123 Main St");
        car.setWard("Ward 1");
        car.setDistrict("District A");
        car.setCityProvince("City X");
        car.setRegistrationPaperUri("s3://documents/registration.pdf");
        car.setCertificateOfInspectionUri("s3://documents/inspection.pdf");
        car.setInsuranceUri("s3://documents/insurance.pdf");
        car.setAccount(account);

        // Mock CarResponse
        CarResponse carResponse = new CarResponse();
        carResponse.setId(carId);
        carResponse.setAddress("Ward 1, District A, City X");

        // Mock repository và service
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        lenient().when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        lenient().when(bookingRepository.isCarBookedByAccount(carId, accountId)).thenReturn(true);
        lenient().when(bookingRepository.countCompletedBookingsByCar(carId)).thenReturn(8L);
        lenient().when(carMapper.toCarResponse(car)).thenReturn(carResponse); // ✅ Fix mock

        // Act
        CarResponse result = carService.getCarById(carId);

        // Assert
        assertNotNull(result);
        assertEquals(carId, result.getId()); // ✅ Fix lỗi assert
        assertEquals("City X, District A, Ward 1, 123 Main St", result.getAddress());

        // Verify đúng tham số
        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).toCarResponse(car);
    }


    @Test
    void testGetCarDetail_WhenCarDoesNotExist_ShouldThrowException() {
        String carId = "car-999";
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> carService.getCarDetail(carId));

        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void testEditCar_CarNotFound() {
        String carId = "non-existent-car-id";
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> carService.editCar(editCarRequest, carId));
        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void editCar_ShouldUpdateImages_WhenNewImagesProvided() throws Exception {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest();
        request.setCarImageFront(mock(MultipartFile.class)); // Giả lập ảnh mới

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), anyString()); // Kiểm tra ảnh được upload
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldNotUploadImages_WhenNoNewImagesProvided() throws Exception {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest(); // Không set ảnh nào

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService, never()).uploadFile(any(), anyString()); // Không gọi uploadFile
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldUploadOnlyFrontImage_WhenOnlyFrontImageProvided() throws Exception {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest();
        request.setCarImageFront(mock(MultipartFile.class)); // Chỉ có ảnh trước

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("front"));
        verify(fileService, never()).uploadFile(any(), contains("back"));
        verify(fileService, never()).uploadFile(any(), contains("left"));
        verify(fileService, never()).uploadFile(any(), contains("right"));
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldUploadOnlyBackImage_WhenOnlyBackImageProvided() throws Exception {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest();
        request.setCarImageBack(mock(MultipartFile.class)); // Chỉ có ảnh trước

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("back"));
        verify(fileService, never()).uploadFile(any(), contains("front"));
        verify(fileService, never()).uploadFile(any(), contains("left"));
        verify(fileService, never()).uploadFile(any(), contains("right"));
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldUploadOnlyLeftImage_WhenOnlyLeftImageProvided() throws Exception {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest();
        request.setCarImageLeft(mock(MultipartFile.class)); // Chỉ có ảnh trước

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("left"));
        verify(fileService, never()).uploadFile(any(), contains("back"));
        verify(fileService, never()).uploadFile(any(), contains("front"));
        verify(fileService, never()).uploadFile(any(), contains("right"));
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldUploadOnlyRightImage_WhenOnlyRightImageProvided() throws Exception {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);

        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest();
        request.setCarImageRight(mock(MultipartFile.class)); // Chỉ có ảnh trước

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("right"));
        verify(fileService, never()).uploadFile(any(), contains("back"));
        verify(fileService, never()).uploadFile(any(), contains("front"));
        verify(fileService, never()).uploadFile(any(), contains("left"));
        verify(carRepository).save(car);
    }


    @Test
    void editCar_ShouldKeepExistingStatus_WhenStatusIsNull() {
        // Given
        String carId = "car123";
        String accountId = "user123";

        Account account = new Account();
        account.setId(accountId);
        Car car = new Car();
        car.setId(carId);
        car.setAccount(account);
        car.setStatus(ECarStatus.NOT_VERIFIED); // Trạng thái ban đầu

        EditCarRequest request = new EditCarRequest();
        request.setStatus(null); // Không gửi trạng thái mới

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        assertEquals(ECarStatus.NOT_VERIFIED, car.getStatus()); // Trạng thái không đổi
    }

    @Test
    void testCarAvailable_NoBookings() {
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);

        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(List.of());

        assertTrue(carService.isCarAvailable(carId, pickUpTime, dropOffTime));
    }

    @Test
    void testCarAvailable_AllCancelledOrPendingDeposit() {
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);

        Booking booking1 = new Booking();
        booking1.setStatus(EBookingStatus.CANCELLED);

        Booking booking2 = new Booking();
        booking2.setStatus(EBookingStatus.PENDING_DEPOSIT);

        List<Booking> bookings = List.of(booking1, booking2);

        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(bookings);

        assertTrue(carService.isCarAvailable(carId, pickUpTime, dropOffTime));
    }


    @Test
    void testCarNotAvailable_HasActiveBooking() {
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);

        Booking booking = new Booking();
        booking.setStatus(EBookingStatus.CONFIRMED); // Dùng setter thay vì constructor

        List<Booking> bookings = List.of(booking);

        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(bookings);

        assertFalse(carService.isCarAvailable(carId, pickUpTime, dropOffTime));
    }


    @Test
    void testCarNotBooked_NoBookingExists() {
        String carId = "car123";
        String accountId = "user456";

        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(anyString(), anyString(), anyList()))
                .thenReturn(false);

        assertFalse(carService.isCarBooked(carId, accountId));
    }

    @Test
    void testCarNotBooked_BookingWithInvalidStatus() {
        String carId = "car123";
        String accountId = "user456";

        // Giả sử tồn tại booking nhưng không có trạng thái hợp lệ
        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(anyString(), anyString(), anyList()))
                .thenReturn(false);

        assertFalse(carService.isCarBooked(carId, accountId));
    }

    @Test
    void testCarBooked_WithValidStatus() {
        String carId = "car123";
        String accountId = "user456";

        // Giả sử tồn tại booking với trạng thái hợp lệ
        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(anyString(), anyString(), anyList()))
                .thenReturn(true);

        assertTrue(carService.isCarBooked(carId, accountId));
    }

}