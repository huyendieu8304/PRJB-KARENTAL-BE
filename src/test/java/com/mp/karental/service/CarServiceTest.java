package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.car.AddCarRequest;
import com.mp.karental.dto.request.car.CarDetailRequest;
import com.mp.karental.dto.request.car.EditCarRequest;
import com.mp.karental.dto.request.car.SearchCarRequest;
import com.mp.karental.dto.response.car.CarDetailResponse;
import com.mp.karental.dto.response.car.CarDocumentsResponse;
import com.mp.karental.dto.response.car.CarResponse;
import com.mp.karental.dto.response.car.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.*;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.util.RedisUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
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
    private FeedbackRepository feedbackRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private FileService fileService;
    @Mock
    private AddCarRequest addCarRequest;
    @Mock
    private EditCarRequest editCarRequest;
    @Mock
    private EmailService emailService;
    @Mock
    private RedisUtil redisUtil;

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
    void editCar_ShouldUploadOnlyCertificateImage_WhenOnlyCertificateImageProvided() throws Exception  {
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
        request.setCertificateOfInspection(mock(MultipartFile.class));

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("certificate-of-inspection"));
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldUploadOnlyInsuranceImage_WhenOnlyInsuranceImageProvided() throws Exception {
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
        request.setInsurance(mock(MultipartFile.class));

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("insurance"));
        verify(carRepository).save(car);
    }

    @Test
    void editCar_ShouldUploadOnlyRegistrationImage_WhenOnlyRegistrationImageProvided() throws Exception {
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
        request.setRegistrationPaper(mock(MultipartFile.class));

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), contains("registration-paper"));
        verify(carRepository).save(car);
    }

    @Test
    void testEditCar_CancelPendingDepositsWhenCarStopped() {
        // Given
        String carId = "car123";
        String accountId = "account456";
        EditCarRequest request = new EditCarRequest();
        request.setStatus(ECarStatus.STOPPED);

        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);

        Account owner = new Account();
        owner.setId(accountId);
        car.setAccount(owner);

        Booking booking = new Booking();
        booking.setBookingNumber("BOOK123");
        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        booking.setCar(car);

        Account customer = new Account();
        customer.setEmail("customer@example.com");
        booking.setAccount(customer);


        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);


        lenient().when(carRepository.findById(carId)).thenReturn(Optional.of(car));


        lenient().when(bookingRepository.findByCarIdAndStatus(carId, EBookingStatus.PENDING_DEPOSIT))
                .thenReturn(List.of(booking));


        when(carMapper.toCarResponse(any(Car.class))).thenAnswer(invocation -> {
            Car updatedCar = invocation.getArgument(0);
            CarResponse response = new CarResponse();
            response.setLicensePlate(updatedCar.getLicensePlate());
            response.setAddress(String.join(", ",
                    updatedCar.getCityProvince(),
                    updatedCar.getDistrict(),
                    updatedCar.getWard(),
                    updatedCar.getHouseNumberStreet()));
            return response;
        });


        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));


        carService.editCar(request, carId);


        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);


        verify(emailService).sendCancelledBookingEmail(
                eq("customer@example.com"),
                anyString(),
                contains("was automatically canceled")
        );


        verify(redisUtil).removeCachePendingDepositBooking("BOOK123");
    }


    @Test
    void testEditCar_CarCurrentlyBooked_CannotStopCar() {
        // Given
        String carId = "car123";
        String accountId = "account456";
        EditCarRequest request = new EditCarRequest();
        request.setStatus(ECarStatus.STOPPED);

        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);

        Account owner = new Account();
        owner.setId(accountId);
        car.setAccount(owner);

        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));


        when(bookingRepository.hasActiveBooking(eq(carId), anyList())).thenReturn(true);

        // When & Then
        AppException thrown = assertThrows(AppException.class, () -> {
            carService.editCar(request, carId);
        });

        assertEquals(ErrorCode.CAR_CANNOT_STOPPED, thrown.getErrorCode());
    }

    @Test
    void testIsCarAvailable_CarNotVerified_ThrowsException() {
        // Given
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);

        lenient().when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.STOPPED);
        lenient().when(carRepository.findById(anyString()))
                .thenReturn(Optional.of(car));

        // When
        boolean available = carService.isCarAvailable(carId, pickUpTime, dropOffTime);

        // Then
        assertFalse(available);
    }
    @Test
    void testIsCarAvailable_ActiveBookingsExist_ReturnsFalse() {
        // Given
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);
        Booking booking = new Booking();
        booking.setBookingNumber("b1");
        booking.setStatus(EBookingStatus.IN_PROGRESS);
        Booking booking1 = new Booking();
        booking1.setBookingNumber("b2");
        booking1.setStatus(EBookingStatus.CONFIRMED);
        List<Booking> mockBookings = List.of(
                booking,
                booking1
        );

        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(mockBookings);
        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);
        when(carRepository.findById(anyString()))
                .thenReturn(Optional.of(car));

        // When
        boolean available = carService.isCarAvailable(carId, pickUpTime, dropOffTime);

        // Then
        assertFalse(available);
    }

    @Test
    void testIsCarAvailable_AllCancelledOrPendingDeposit_ReturnsTrue() {
        // Given
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);
        Booking booking = new Booking();
        booking.setBookingNumber("b1");
        booking.setStatus(EBookingStatus.CANCELLED);
        Booking booking1 = new Booking();
        booking1.setBookingNumber("b2");
        booking1.setStatus(EBookingStatus.PENDING_DEPOSIT);
        List<Booking> mockBookings = List.of(
                booking,
                booking1
        );

        lenient().when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(mockBookings);
        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);
        lenient().when(carRepository.findById(anyString()))
                .thenReturn(Optional.of(car));

        // When
        boolean available = carService.isCarAvailable(carId, pickUpTime, dropOffTime);

        // Then
        assertTrue(available);
    }

    @Test
    void testIsCarAvailable_NoBookings_ReturnsTrue() {
        // Given
        String carId = "car123";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(3);

        lenient().when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);
        lenient().when(carRepository.findById(anyString()))
                .thenReturn(Optional.of(car));

        // When
        boolean available = carService.isCarAvailable(carId, pickUpTime, dropOffTime);

        // Then
        assertTrue(available);
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
    void editCarStatus_validRequest_success() throws AppException {
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
    void editCarStatus_invalidTransition_shouldThrowException() {
        // Mock SecurityUtil to return a valid account ID
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock existing account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock existing car in the database
        Car existingCar = new Car();
        existingCar.setId("car-123");
        existingCar.setStatus(ECarStatus.STOPPED);
        existingCar.setAccount(mockAccount);

        when(carRepository.findById("car-123")).thenReturn(Optional.of(existingCar));


        EditCarRequest editCarRequest = new EditCarRequest();
        editCarRequest.setStatus(ECarStatus.VERIFIED);


        AppException exception = assertThrows(AppException.class,
                () -> carService.editCar(editCarRequest, "car-123"));


        assertEquals(ErrorCode.INVALID_CAR_STATUS_CHANGE, exception.getErrorCode());
    }

    @Test
    void editCarStatus_invalidTransition_shouldThrowException2() {
        // Mock SecurityUtil to return a valid account ID
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock existing account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock existing car in the database
        Car existingCar = new Car();
        existingCar.setId("car-123");
        existingCar.setStatus(ECarStatus.NOT_VERIFIED);
        existingCar.setAccount(mockAccount);

        when(carRepository.findById("car-123")).thenReturn(Optional.of(existingCar));


        EditCarRequest editCarRequest = new EditCarRequest();
        editCarRequest.setStatus(ECarStatus.VERIFIED);


        AppException exception = assertThrows(AppException.class,
                () -> carService.editCar(editCarRequest, "car-123"));


        assertEquals(ErrorCode.INVALID_CAR_STATUS_CHANGE, exception.getErrorCode());
    }



    @Test
    void editCar_shouldThrowException_whenCarNotFound() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        when(carRepository.findById("car-999")).thenReturn(Optional.empty());

        EditCarRequest editCarRequest = new EditCarRequest();
        editCarRequest.setStatus(ECarStatus.NOT_VERIFIED);

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
    void testGetCarDetail_WhenCarExistsAndBooked_ShouldReturnCarResponseWithFullAddress() {
        // Given
        Account account = new Account();
        String carId = "car-456";
        String accountId = "user-123";
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


        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        lenient().when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        lenient().when(bookingRepository.isCarBookedByAccount(carId, accountId)).thenReturn(true);
        lenient().when(bookingRepository.countCompletedBookingsByCar(carId)).thenReturn(8L);
        lenient().when(carMapper.toCarResponse(car)).thenReturn(carResponse); // ✅ Fix mock

        // Act
        CarResponse result = carService.getCarById(carId);

        // Assert
        assertNotNull(result);
        assertEquals(carId, result.getId());
        assertEquals("City X, District A, Ward 1, 123 Main St", result.getAddress());


        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).toCarResponse(car);
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
        request.setCarImageFront(mock(MultipartFile.class));

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService).uploadFile(any(MultipartFile.class), anyString());
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

        EditCarRequest request = new EditCarRequest();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        verify(fileService, never()).uploadFile(any(), anyString());
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
        request.setCarImageFront(mock(MultipartFile.class));

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
        request.setCarImageBack(mock(MultipartFile.class));

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
        request.setCarImageLeft(mock(MultipartFile.class));

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
        request.setCarImageRight(mock(MultipartFile.class));

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
        car.setStatus(ECarStatus.NOT_VERIFIED);

        EditCarRequest request = new EditCarRequest();
        request.setStatus(null);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(new CarResponse());

        // When
        CarResponse response = carService.editCar(request, carId);

        // Then
        assertNotNull(response);
        assertEquals(ECarStatus.NOT_VERIFIED, car.getStatus());
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


        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(anyString(), anyString(), anyList()))
                .thenReturn(false);

        assertFalse(carService.isCarBooked(carId, accountId));
    }

    @Test
    void testCarBooked_WithValidStatus() {
        String carId = "car123";
        String accountId = "user456";


        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(anyString(), anyString(), anyList()))
                .thenReturn(true);

        assertTrue(carService.isCarBooked(carId, accountId));
    }
    @Test
    void searchCars_ReturnsEmptyList() {
        // Mock request
        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productionYear"));


        Page<Car> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(carRepository.findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable)).thenReturn(emptyPage);

        // Call service
        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");


        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());


        verify(carRepository, times(1)).findVerifiedCarsByAddress(ECarStatus.VERIFIED, "Hanoi", pageable);
    }


    @Test
    void getCarsByUserId_ShouldUseDefaultSize_WhenSizeInvalid() {

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
    void isCarBooked_ShouldReturnTrue_WhenBookingExists() {

        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(any(), any(), any()))
                .thenReturn(true);

        boolean result = carService.isCarBooked("car123", "user123");

        assertTrue(result);
    }

    @Test
    void searchCars_ShouldReturnEmpty_WhenNoVerifiedCars() {

        when(carRepository.findVerifiedCarsByAddress(any(), any(), any())).thenReturn(Page.empty());

        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");

        assertEquals(0, result.getTotalElements());
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

    @Test
    void testGetAllCarsForOperator_ReturnsPagedCars() {
        // Given
        int page = 0;
        int size = 5;
        String sort = "updatedAt,desc";
        ECarStatus status = ECarStatus.VERIFIED;

        Car car1 = new Car();
        car1.setId("car1");
        car1.setWard("Ward 1");
        car1.setCityProvince("Hanoi");
        car1.setCarImageFront("image1.jpg");
        car1.setCarImageBack("image2.jpg");
        car1.setCarImageLeft("image3.jpg");
        car1.setCarImageRight("image4.jpg");

        Page<Car> carPage = new PageImpl<>(List.of(car1));

        when(carRepository.findCars(eq(status), any(Pageable.class))).thenReturn(carPage);
        when(carMapper.toCarThumbnailResponse(any(Car.class))).thenReturn(new CarThumbnailResponse());
        when(fileService.getFileUrl(anyString())).thenReturn("https://example.com/image.jpg");
        when(bookingRepository.countCompletedBookingsByCar(anyString())).thenReturn(Long.valueOf(10));

        // When
        Page<CarThumbnailResponse> response = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(carRepository).findCars(eq(status), any(Pageable.class));
        verify(fileService, times(4)).getFileUrl(anyString()); // Check image retrieval
    }

    @Test
    void testGetAllCarsForOperator_NoCarsFound() {
        // Given
        int page = 0;
        int size = 5;
        String sort = "updatedAt,desc";
        ECarStatus status = ECarStatus.NOT_VERIFIED;

        when(carRepository.findCars(eq(status), any(Pageable.class))).thenReturn(Page.empty());

        // When
        Page<CarThumbnailResponse> response = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        verify(carRepository).findCars(eq(status), any(Pageable.class));
    }

    @Test
    void testGetAllCarsForOperator_SortingByUpdatedAtAsc() {
        // Given
        int page = 0;
        int size = 5;
        String sort = "updatedAt,asc";
        ECarStatus status = ECarStatus.VERIFIED;

        when(carRepository.findCars(eq(status), any(Pageable.class))).thenReturn(Page.empty());

        // When
        Page<CarThumbnailResponse> response = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        verify(carRepository).findCars(eq(status), any(Pageable.class));
    }

    @Test
    void testGetAllCarsForOperator_DefaultSortingWhenSortIsNull() {
        // Given
        int page = 0;
        int size = 5;
        String sort = null;
        ECarStatus status = ECarStatus.VERIFIED;

        when(carRepository.findCars(eq(status), any(Pageable.class))).thenReturn(Page.empty());

        // When
        Page<CarThumbnailResponse> response = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        verify(carRepository).findCars(eq(status), any(Pageable.class));
    }
    @Test
    void testGetAllCarsForOperator_NoStatus_DefaultSort() {
        // Given
        int page = 0;
        int size = 10;
        String sort = null;
        ECarStatus status = null;

        Car car = new Car();
        car.setId("car123");
        car.setWard("District A");
        car.setCityProvince("City X");
        car.setCarImageFront("front.jpg");
        car.setCarImageBack("back.jpg");
        car.setCarImageLeft("left.jpg");
        car.setCarImageRight("right.jpg");

        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findCars(isNull(), any(Pageable.class))).thenReturn(carPage);

        CarThumbnailResponse response = new CarThumbnailResponse();
        response.setAddress(car.getWard() + ", " + car.getCityProvince());
        response.setCarImageFront("http://image.com/front.jpg");
        response.setCarImageBack("http://image.com/back.jpg");
        response.setCarImageLeft("http://image.com/left.jpg");
        response.setCarImageRight("http://image.com/right.jpg");
        response.setNoOfRides(10);
        response.setAverageRatingByCar(4.5);

        when(carMapper.toCarThumbnailResponse(any(Car.class))).thenReturn(response);

        // When
        Page<CarThumbnailResponse> result = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(carRepository).findCars(isNull(), any(Pageable.class));
        verify(carMapper).toCarThumbnailResponse(any(Car.class));
    }


    @Test
    void testGetAllCarsForOperator_WithStatus_SortingAsc() {
        // Given
        int page = 0;
        int size = 5;
        String sort = "updatedAt,asc";
        ECarStatus status = ECarStatus.VERIFIED;

        Car car = new Car();
        car.setId("car123");
        car.setWard("District A");
        car.setCityProvince("City X");

        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findCars(eq(status), any(Pageable.class))).thenReturn(carPage);

        CarThumbnailResponse response = new CarThumbnailResponse();
        response.setAddress(car.getWard() + ", " + car.getCityProvince());

        when(carMapper.toCarThumbnailResponse(any())).thenReturn(response);

        // When
        Page<CarThumbnailResponse> result = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(carRepository).findCars(eq(status), any(Pageable.class));
        verify(carMapper).toCarThumbnailResponse(car);
    }

    @Test
    void testGetAllCarsForOperator_InvalidSort_DefaultUsed() {
        // Given
        int page = 1;
        int size = 10;
        String sort = "name,desc";
        ECarStatus status = null;

        Car car = new Car();
        car.setId("car123");

        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findCars(isNull(), any(Pageable.class))).thenReturn(carPage);

        CarThumbnailResponse response = new CarThumbnailResponse();
        response.setAddress("District A, City X");

        when(carMapper.toCarThumbnailResponse(any(Car.class))).thenReturn(response);

        // When
        Page<CarThumbnailResponse> result = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(carRepository).findCars(isNull(), any(Pageable.class));
        verify(carMapper).toCarThumbnailResponse(any(Car.class));
    }

    @Test
    void testGetAllCarsForOperator_EmptyResult() {
        // Given
        int page = 0;
        int size = 10;
        String sort = null;
        ECarStatus status = null;

        Page<Car> carPage = Page.empty();

        when(carRepository.findCars(isNull(), any(Pageable.class))).thenReturn(carPage);

        // When
        Page<CarThumbnailResponse> result = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(carRepository).findCars(isNull(), any(Pageable.class));
        verify(carMapper, never()).toCarThumbnailResponse(any());
    }


    @Test
    void testGetAllCarsForOperator_ValidDataMapping() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "updatedAt,desc";
        ECarStatus status = null;

        Car car = new Car();
        car.setId("car123");
        car.setWard("District A");
        car.setCityProvince("City X");
        car.setCarImageFront("front.jpg");
        car.setCarImageBack("back.jpg");
        car.setCarImageLeft("left.jpg");
        car.setCarImageRight("right.jpg");

        Page<Car> carPage = new PageImpl<>(List.of(car));

        CarThumbnailResponse response = new CarThumbnailResponse();
        response.setAddress(car.getWard() + ", " + car.getCityProvince());
        response.setCarImageFront("http://image.com/front.jpg");
        response.setCarImageBack("http://image.com/back.jpg");
        response.setCarImageLeft("http://image.com/left.jpg");
        response.setCarImageRight("http://image.com/right.jpg");
        response.setNoOfRides(10);
        response.setAverageRatingByCar(4.5);

        when(carRepository.findCars(isNull(), any(Pageable.class))).thenReturn(carPage);
        when(carMapper.toCarThumbnailResponse(car)).thenReturn(response);
        when(fileService.getFileUrl(anyString())).thenReturn("http://image.com");

        // When
        Page<CarThumbnailResponse> result = carService.getAllCarsForOperator(page, size, sort, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("District A, City X", result.getContent().get(0).getAddress());
        verify(fileService, times(4)).getFileUrl(anyString());
        verify(bookingRepository).countCompletedBookingsByCar(car.getId());
    }

    @Test
    void testVerifyCar_Success() {
        // Given
        String carId = "car123";
        Account mockOperator = new Account();
        mockOperator.setId("operator123");

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockOperator);
        Account owner = new Account();
        owner.setId("owner123");
        owner.setEmail("owner@gmail");
        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.NOT_VERIFIED);
        car.setAccount(owner);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.saveAndFlush(any(Car.class))).thenReturn(car);

        // When
        String response = carService.verifyCar(carId);

        // Then
        assertNotNull(response);
        assertEquals("Car has been verified successfully.", response);
        assertEquals(ECarStatus.VERIFIED, car.getStatus());
        verify(carRepository).saveAndFlush(car);
    }


    @Test
    void testVerifyCar_CarNotFound() {
        // Given
        String carId = "invalidCarId";
        Account mockOperator = new Account();
        mockOperator.setId("operator123");

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockOperator);

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> carService.verifyCar(carId));
        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void testVerifyCar_InvalidStatus() {
        // Given
        String carId = "car123";
        Account mockOperator = new Account();
        mockOperator.setId("operator123");

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockOperator);

        Car car = new Car();
        car.setId(carId);
        car.setStatus(ECarStatus.VERIFIED);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> carService.verifyCar(carId));
        assertEquals(ErrorCode.INVALID_CAR_STATUS, exception.getErrorCode());
    }

    @Test
    void testGetCarDocuments_Success() {
        // Given
        String carId = "car123";
        Car car = new Car();
        car.setId(carId);
        car.setRegistrationPaperUri("reg-paper-uri");
        car.setCertificateOfInspectionUri("inspection-uri");
        car.setInsuranceUri("insurance-uri");

        CarDocumentsResponse mockResponse = CarDocumentsResponse.builder()
                .registrationPaperUrl("http://mock-url/reg-paper-uri")
                .certificateOfInspectionUrl("http://mock-url/inspection-uri")
                .insuranceUrl("http://mock-url/insurance-uri")
                .build();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toCarDocumentsResponse(car)).thenReturn(mockResponse);
        when(fileService.getFileUrl("reg-paper-uri")).thenReturn("http://mock-url/reg-paper-uri");
        when(fileService.getFileUrl("inspection-uri")).thenReturn("http://mock-url/inspection-uri");
        when(fileService.getFileUrl("insurance-uri")).thenReturn("http://mock-url/insurance-uri");

        // When
        CarDocumentsResponse result = carService.getCarDocuments(carId);

        // Then
        assertNotNull(result);
        assertEquals("http://mock-url/reg-paper-uri", result.getRegistrationPaperUrl());
        assertEquals("http://mock-url/inspection-uri", result.getCertificateOfInspectionUrl());
        assertEquals("http://mock-url/insurance-uri", result.getInsuranceUrl());

        verify(carRepository).findById(carId);
        verify(fileService, times(3)).getFileUrl(anyString());
    }

    @Test
    void testGetCarDocuments_CarNotFound() {
        // Given
        String carId = "invalidCarId";
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> carService.getCarDocuments(carId));
        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());

        verify(carRepository).findById(carId);
        verifyNoInteractions(fileService);
    }

    @Test
    void searchCars_ShouldReturnEmpty_WhenNoAvailableCars() {
        Car car = new Car();
        car.setId("car123");

        Page<Car> verifiedCars = new PageImpl<>(List.of(car));

        when(carRepository.findVerifiedCarsByAddress(any(), any(), any())).thenReturn(verifiedCars);
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        when(bookingRepository.findActiveBookingsByCarIdAndTimeRange(any(), any(), any()))
                .thenReturn(List.of(new Booking()));

        SearchCarRequest request = new SearchCarRequest();
        request.setAddress("Hanoi");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        Page<CarThumbnailResponse> result = carService.searchCars(request, 0, 10, "productionYear,desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

}