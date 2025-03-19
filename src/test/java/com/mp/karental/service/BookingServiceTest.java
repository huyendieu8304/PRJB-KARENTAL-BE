package com.mp.karental.service;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.booking.BookingListResponse;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.entity.*;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.BookingMapper;
import com.mp.karental.repository.*;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.util.RedisUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.mockito.ArgumentMatchers.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static software.amazon.awssdk.services.s3.endpoints.internal.ParseArn.ACCOUNT_ID;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;
    @Mock
    private TransactionService transactionService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private FileService fileService;

    @Mock
    private CarService carService;

    @Mock
    private MultipartFile mockFile;

    @Mock
    private AccountRepository accountRepository;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;
    private String accountId;

    @BeforeEach
    void setUp() {
        accountId = "user123";
        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("user123");
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void createBooking_MissingFields_ThrowsException3() {
        // Given
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setDriver(true);
        CreateBookingRequest.setDriverFullName("abc");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("");
        CreateBookingRequest.setDriverPhoneNumber("");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        CreateBookingRequest.setDriverDrivingLicense(mockFile);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);
        CreateBookingRequest.setCarId("car123");

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(mockCar);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        lenient().when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
        lenient().when(bookingMapper.toBooking(any())).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);

        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/" + expectedS3Key;

        lenient().when(fileService.getFileUrl(expectedS3Key)).thenReturn(expectedUrl);
        lenient().when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());


        // Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(CreateBookingRequest));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void createBooking_MissingFields_ThrowsException2() {
        // Given
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setDriver(true);
        CreateBookingRequest.setDriverFullName("abc");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        CreateBookingRequest.setDriverDrivingLicense(mockFile);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);
        CreateBookingRequest.setCarId("car123");

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(mockCar);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        lenient().when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
        lenient().when(bookingMapper.toBooking(any())).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);

        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/" + expectedS3Key;

        lenient().when(fileService.getFileUrl(expectedS3Key)).thenReturn(expectedUrl);
        lenient().when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());


        // Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(CreateBookingRequest));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void createBooking_MissingFields_ThrowsException() {
        // Given
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setDriver(true);
        CreateBookingRequest.setDriverFullName("");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        CreateBookingRequest.setDriverDrivingLicense(mockFile);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);
        CreateBookingRequest.setCarId("car123");

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(mockCar);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        lenient().when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
        lenient().when(bookingMapper.toBooking(any())).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);

        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/" + expectedS3Key;

        lenient().when(fileService.getFileUrl(expectedS3Key)).thenReturn(expectedUrl);
        lenient().when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());


        // Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(CreateBookingRequest));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException5() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("null");
        CreateBookingRequest.setDriverDob(LocalDate.of(1,1,1));
        CreateBookingRequest.setDriverNationalId("null");
        CreateBookingRequest.setDriverPhoneNumber(null);
        CreateBookingRequest.setDriverCityProvince("null");
        CreateBookingRequest.setDriverDistrict("null");
        CreateBookingRequest.setDriverWard("null");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });
        // Kiểm tra error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException4() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("null");
        CreateBookingRequest.setDriverDob(LocalDate.of(1,1,1));
        CreateBookingRequest.setDriverNationalId(null);
        CreateBookingRequest.setDriverPhoneNumber("null");
        CreateBookingRequest.setDriverCityProvince("null");
        CreateBookingRequest.setDriverDistrict("null");
        CreateBookingRequest.setDriverWard("null");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });
        // Kiểm tra error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException3() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("null");
        CreateBookingRequest.setDriverDob(null);
        CreateBookingRequest.setDriverNationalId("null");
        CreateBookingRequest.setDriverPhoneNumber("null");
        CreateBookingRequest.setDriverCityProvince("null");
        CreateBookingRequest.setDriverDistrict("null");
        CreateBookingRequest.setDriverWard("null");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });
        // Kiểm tra error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException2() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);



        // Mock file mới (giấy phép lái xe)
        MultipartFile newMockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(newMockFile);

        lenient().when(newMockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileUrl(anyString())).thenReturn("https://s3-bucket.com/dummy-url.jpg");


        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });
        // Kiểm tra error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @ParameterizedTest
    @EnumSource(value = EBookingStatus.class, names = {"IN_PROGRESS", "PENDING_PAYMENT", "COMPLETED", "CANCELLED"})
    void editBooking_InvalidStatus_ThrowsException(EBookingStatus status) {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setCarId("car123");

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock booking có trạng thái không cho phép sửa
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        Car car = new Car();
        car.setId("car123");
        existingBooking.setCar(car);
        existingBooking.setStatus(status); // Các trạng thái không cho phép sửa

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });

        // Kiểm tra error code
        assertEquals(ErrorCode.BOOKING_CANNOT_BE_EDITED, exception.getErrorCode());
    }

    @Test
    void editBooking_Success_WithNewDriverLicense() throws AppException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        CreateBookingRequest.setDriverWard("Kim Mã");
        CreateBookingRequest.setDriverEmail("test@gmail.com");
        CreateBookingRequest.setDriverHouseNumberStreet("123 Đường ABC");


        // Mock file mới (giấy phép lái xe)
        MultipartFile newMockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(newMockFile);

        lenient().when(newMockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileUrl(anyString())).thenReturn("https://s3-bucket.com/dummy-url.jpg");


        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Thực thi service
        BookingResponse response = bookingService.editBooking(CreateBookingRequest, bookingNumber);

        // Kiểm tra kết quả
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedUrl, response.getDriverDrivingLicenseUrl());
    }


    @Test
    void editBooking_Success_WithOldDriverLicense() throws AppException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        CreateBookingRequest.setDriverWard("Kim Mã");
        CreateBookingRequest.setDriverEmail("test@gmail.com");
        CreateBookingRequest.setDriverHouseNumberStreet("123 Đường ABC");


        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(existingBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Thực thi service
        BookingResponse response = bookingService.editBooking(CreateBookingRequest, bookingNumber);

        // Kiểm tra kết quả
        assertNotNull(response, "Response should not be null");
        assertEquals("https://s3-bucket.com/old-license.jpg", response.getDriverDrivingLicenseUrl());
    }

    @Test
    void editBooking_Failed_WithNewDriverLicense() throws AppException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        CreateBookingRequest.setDriverWard("Kim Mã");
        CreateBookingRequest.setDriverEmail("test@gmail.com");
        CreateBookingRequest.setDriverHouseNumberStreet("123 Đường ABC");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("user/license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("user/license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(existingBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });
        // Kiểm tra error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        CreateBookingRequest.setDriverWard("Kim Mã");


        // Mock file mới (giấy phép lái xe)
        MultipartFile newMockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(newMockFile);

        lenient().when(newMockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileUrl(anyString())).thenReturn("https://s3-bucket.com/dummy-url.jpg");


        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock file upload
        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";

        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");


        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });
        // Kiểm tra error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_CarNotMatch_ThrowsException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit (Car ID khác với Booking)
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setCarId("car456"); // Xe khác

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock booking có xe khác
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        Car car = new Car();
        car.setId("car123");
        existingBooking.setCar(car); // Xe cũ
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });

        // Kiểm tra error code
        assertEquals(ErrorCode.CAR_NOT_AVAILABLE, exception.getErrorCode());
    }

    @Test
    void editBooking_NotOwner_ThrowsException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setCarId("car123");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock account khác (chủ sở hữu booking)
        Account anotherAccount = new Account();
        anotherAccount.setId("otherUser");

        // Mock booking thuộc về người khác
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(anotherAccount); // Khác với `mockAccount`
        Car car = new Car();
        car.setId("car123");
        existingBooking.setCar(car);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });

        // Kiểm tra error code
        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }

    @Test
    void editBooking_BookingNotFound_ThrowsException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        CreateBookingRequest.setDriverFullName("Updated Name");
        CreateBookingRequest.setDriverNationalId("0987654321");
        CreateBookingRequest.setDriverPhoneNumber("0771234567");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId("car123");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Gọi service và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(CreateBookingRequest, bookingNumber);
        });

        // Kiểm tra error code
        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());

        // Kiểm tra phương thức đã được gọi đúng số lần
        verify(bookingRepository, times(1)).findBookingByBookingNumber(bookingNumber);
    }

    @Test
    void editBooking_Success() throws AppException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setDriverFullName("Updated Name");
        CreateBookingRequest.setDriverNationalId("0987654321");
        CreateBookingRequest.setDriverPhoneNumber("0771234567");

        // Mock account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        // Mock car
        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);

        CreateBookingRequest.setCarId(mockCar.getId());

        // Mock booking hiện tại trong DB
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0));
        existingBooking.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0));
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock mapper
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking updatedBooking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setPickUpTime(updatedBooking.getPickUpTime());
            response.setDropOffTime(updatedBooking.getDropOffTime());
            response.setCarId(updatedBooking.getCar().getId());
            response.setDriverDrivingLicenseUrl(updatedBooking.getDriverDrivingLicenseUri());
            return response;
        });

        // Thực thi service
        BookingResponse response = bookingService.editBooking(CreateBookingRequest, bookingNumber);
        System.out.println("Booking response"+ response);
        // Kiểm tra kết quả
        assertNotNull(response, "Response should not be null");
        assertEquals(mockCar.getId(), response.getCarId());

    }

    @Test
    void testCreateBooking_WithDriver() throws AppException {
        // Given
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setDriver(true);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        CreateBookingRequest.setDriverWard("Kim Mã");
        CreateBookingRequest.setDriverEmail("test@gmail.com");
        CreateBookingRequest.setDriverHouseNumberStreet("123 Đường ABC");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(mockFile);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        CreateBookingRequest.setDriverDrivingLicense(mockFile);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);
        CreateBookingRequest.setCarId("car123");

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(mockCar);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
        when(bookingMapper.toBooking(any())).thenReturn(booking);
        when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);

        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/" + expectedS3Key;

        when(fileService.getFileUrl(expectedS3Key)).thenReturn(expectedUrl);
        when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());

        // When
        BookingResponse response = bookingService.createBooking(CreateBookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(expectedUrl, response.getDriverDrivingLicenseUrl());
        verify(fileService).uploadFile(mockFile, expectedS3Key);
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void testCreateBooking_WithDriver_ThrowException() throws AppException {
        // Given
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setDriver(true);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverCityProvince("Hà Nội");
        CreateBookingRequest.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        CreateBookingRequest.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        CreateBookingRequest.setDriverDrivingLicense(mockFile);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car mockCar = new Car();
        mockCar.setId("car123");
        mockCar.setDeposit(5000);
        mockCar.setBasePrice(2000);
        CreateBookingRequest.setCarId("car123");

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(mockCar);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        lenient().when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
        lenient().when(bookingMapper.toBooking(any())).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);

        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
        String expectedUrl = "https://s3-bucket.com/" + expectedS3Key;

        lenient().when(fileService.getFileUrl(expectedS3Key)).thenReturn(expectedUrl);
        lenient().when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());


        // Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(CreateBookingRequest));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void createBooking_Success() throws AppException {
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);
        CreateBookingRequest.setDriverFullName("test");
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0886980035");

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car car = new Car();
        car.setId("car123");
        car.setDeposit(5000);
        car.setBasePrice(2000);

        MultipartFile mockDrivingLicense = mock(MultipartFile.class);
        lenient().when(mockDrivingLicense.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(any(MultipartFile.class))).thenReturn(".jpg");
        CreateBookingRequest.setDriverDrivingLicense(mockDrivingLicense);

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);
        lenient().when(bookingMapper.toBooking(any())).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        lenient().when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());

        lenient().doAnswer(invocation -> {
            wallet.setBalance(wallet.getBalance() - car.getDeposit());
            return null;
        }).when(transactionService).payDeposit(accountId, car.getDeposit(), booking);


        BookingResponse response = bookingService.createBooking(CreateBookingRequest);

        assertNotNull(response);
        assertEquals(5000, wallet.getBalance());
        verify(transactionService, times(1)).payDeposit(accountId, car.getDeposit(), booking);
        verify(walletRepository, times(1)).save(wallet);
        verify(bookingRepository, times(1)).save(any());

    }

    @Test
    void createBooking_WhenDriverInfoInvalid_ShouldThrowAppException() {
        // Given
        String accountId = "user123";

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);
        CreateBookingRequest.setDriver(true);

        // Intentionally set invalid driver info
        CreateBookingRequest.setDriverFullName(null);  // Invalid
        CreateBookingRequest.setDriverNationalId(null);  // Invalid
        CreateBookingRequest.setDriverPhoneNumber("0886980035");  // Valid, but the other two are invalid

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);
        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        Car car = new Car();
        car.setId("car123");
        car.setDeposit(5000);
        car.setBasePrice(2000);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);
        lenient().when(bookingMapper.toBooking(any())).thenReturn(new Booking());
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK123");

        // The actual service method call for createBooking
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.createBooking(CreateBookingRequest));

        // Assert that the exception is of the correct type and contains the correct error code
        assertEquals(ErrorCode.INVALID_DRIVER_INFO.name(), exception.getErrorCode().name());
    }

    @Test
    void createBooking_CarNotFound_ThrowsException() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(18).withMinute(0));

        String accountId = "user123";

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockAccount);

        when(carRepository.findById("car123")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void createBooking_WalletNotFound_ThrowsException() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(10));
        request.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(18));

        String accountId = "user123";

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockAccount);

        Car car = new Car();
        car.setId("car123");
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        when(walletRepository.findById(accountId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void createBooking_CarNotAvailable_ThrowsException() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(10));
        request.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(18));

        String accountId = "user123";

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockAccount);

        Car car = new Car();
        car.setId("car123");
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));

        when(carService.isCarAvailable("car123", request.getPickUpTime(), request.getDropOffTime()))
                .thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.CAR_NOT_AVAILABLE, exception.getErrorCode());
    }

    @Test
    void createBooking_WhenWalletHasEnoughBalance_ShouldSetStatusWaitingConfirm() {

        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now().plusDays(1);
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("1");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        CreateBookingRequest.setPickUpTime(pickUpTime);
        CreateBookingRequest.setDropOffTime(dropOffTime);
        CreateBookingRequest.setDriverFullName("Test User");
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockAccount);
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Car car = new Car();
        car.setId("1");
        car.setDeposit(1000L);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(5000L);

        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(car.getId(), CreateBookingRequest.getPickUpTime(), CreateBookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            CreateBookingRequest request = invocation.getArgument(0);
            Booking mappedBooking = new Booking();
            mappedBooking.setPickUpTime(request.getPickUpTime());
            mappedBooking.setDropOffTime(request.getDropOffTime());
            mappedBooking.setPaymentType(request.getPaymentType());
            mappedBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
            mappedBooking.setCar(car);
            return mappedBooking;
        });

        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setBookingNumber("B123");
            response.setCarId(booking.getCar().getId());
            response.setStatus(booking.getStatus());
            response.setPickUpTime(booking.getPickUpTime());
            response.setDropOffTime(booking.getDropOffTime());
            response.setTotalPrice(2000L);
            response.setDeposit(booking.getCar().getDeposit());
            response.setPaymentType(EPaymentType.WALLET);
            response.setDriverDrivingLicenseUrl("dummyUrl");
            return response;
        });

        doAnswer(invocation -> {
            wallet.setBalance(wallet.getBalance() - car.getDeposit());
            return null;
        }).when(transactionService).payDeposit(eq(accountId), eq(car.getDeposit()), any());

        // When
        BookingResponse response = bookingService.createBooking(CreateBookingRequest);

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRMED, response.getStatus());
        assertEquals(4000L, wallet.getBalance());
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());

        verify(walletRepository, atMostOnce()).save(any());

        verify(transactionService).payDeposit(eq(accountId), eq(car.getDeposit()), any());

    }

    @Test
    void createBooking_WhenWalletHasNotEnoughBalance_ShouldSetStatusPendingDeposit() {
        // Given
        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now().plusDays(1);
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("1");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        CreateBookingRequest.setPickUpTime(pickUpTime);
        CreateBookingRequest.setDropOffTime(dropOffTime);
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverFullName("Test User");

        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2000, 1, 1));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0987654321");
        mockProfile.setCityProvince("Hà Nội");
        mockProfile.setDistrict("Ba Đình");
        mockProfile.setWard("Kim Mã");
        mockProfile.setHouseNumberStreet("123 Đường ABC");
        mockProfile.setDrivingLicenseUri("license.jpg");

        mockAccount.setProfile(mockProfile);

        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockAccount);
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Car car = new Car();
        car.setId("1");
        car.setDeposit(1000L);

        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(500L);

        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(car.getId(), CreateBookingRequest.getPickUpTime(), CreateBookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            CreateBookingRequest request = invocation.getArgument(0);
            Booking mappedBooking = new Booking();
            mappedBooking.setPickUpTime(request.getPickUpTime());
            mappedBooking.setDropOffTime(request.getDropOffTime());
            mappedBooking.setPaymentType(request.getPaymentType());
            mappedBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
            mappedBooking.setCar(car);
            return mappedBooking;
        });

        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setBookingNumber("B123");
            response.setCarId(booking.getCar().getId());
            response.setStatus(booking.getStatus());
            response.setPickUpTime(booking.getPickUpTime());
            response.setDropOffTime(booking.getDropOffTime());
            response.setTotalPrice(0L);
            response.setDeposit(booking.getCar().getDeposit());
            response.setPaymentType(EPaymentType.WALLET);
            response.setDriverDrivingLicenseUrl("dummyUrl");
            return response;
        });

        // When
        BookingResponse response = bookingService.createBooking(CreateBookingRequest);

        // Then
        assertEquals(EBookingStatus.PENDING_DEPOSIT, response.getStatus());
        assertEquals(500L, wallet.getBalance(), "Số dư ví không bị thay đổi vì chưa đủ tiền");
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());

        verify(transactionService, never()).payDeposit(any(), anyLong(), any());

        verify(walletRepository, never()).save(any());
    }

    @Test
    void createBooking_WhenPaymentByCashOrBankTransfer_ShouldSetStatusPendingDeposit() {

        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("1");
        CreateBookingRequest.setPaymentType(EPaymentType.CASH);
        CreateBookingRequest.setPickUpTime(pickUpTime);
        CreateBookingRequest.setDropOffTime(dropOffTime);
        CreateBookingRequest.setDriverNationalId("1234567890");
        CreateBookingRequest.setDriverPhoneNumber("0987654321");
        CreateBookingRequest.setDriverFullName("Test User");


        UserProfile userProfile = new UserProfile();
        userProfile.setFullName("Test User");
        userProfile.setDob(LocalDate.of(2000, 1, 1));
        userProfile.setNationalId("1234567890");
        userProfile.setPhoneNumber("0987654321");
        userProfile.setCityProvince("Hà Nội");
        userProfile.setDistrict("Ba Đình");
        userProfile.setWard("Kim Mã");
        userProfile.setHouseNumberStreet("123 Đường ABC");
        userProfile.setDrivingLicenseUri("license.jpg");

        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setProfile(userProfile);

        Car car = new Car();
        car.setId("1");
        car.setDeposit(1000L);

        Wallet wallet = new Wallet();
        wallet.setBalance(5000L);

        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);

        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(car.getId(), CreateBookingRequest.getPickUpTime(), CreateBookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            CreateBookingRequest request = invocation.getArgument(0);
            Booking mappedBooking = new Booking();
            mappedBooking.setPickUpTime(request.getPickUpTime());
            mappedBooking.setDropOffTime(request.getDropOffTime());
            mappedBooking.setPaymentType(request.getPaymentType());
            mappedBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
            mappedBooking.setCar(car);
            return mappedBooking;
        });


        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));


        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setBookingNumber("B123");
            response.setCarId(booking.getCar().getId());
            response.setStatus(booking.getStatus());
            response.setPickUpTime(booking.getPickUpTime());
            response.setDropOffTime(booking.getDropOffTime());
            response.setTotalPrice(0L);
            response.setDeposit(booking.getCar().getDeposit());
            response.setPaymentType(EPaymentType.CASH);
            response.setDriverDrivingLicenseUrl("dummyUrl");
            return response;
        });

        // When
        BookingResponse response = bookingService.createBooking(CreateBookingRequest);


        // Then
        assertEquals(EBookingStatus.PENDING_DEPOSIT, response.getStatus());
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());
    }

    @Test
    void createBooking_ProfileIncomplete_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("Test User");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId(null);
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("Tỉnh Hà Giang");
        mockProfile.setDistrict("Thành phố Hà Giang");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete1_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName(null);
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("Tỉnh Hà Giang");
        mockProfile.setDistrict("Thành phố Hà Giang");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete2_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(null);
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("Tỉnh Hà Giang");
        mockProfile.setDistrict("Thành phố Hà Giang");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete4_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince(null);
        mockProfile.setDistrict("Thành phố Hà Giang");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete3_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName(null);
        mockProfile.setDob(null);
        mockProfile.setNationalId(null);
        mockProfile.setPhoneNumber(null);
        mockProfile.setCityProvince(null);
        mockProfile.setDistrict(null);
        mockProfile.setWard(null);
        mockProfile.setHouseNumberStreet(null);
        mockProfile.setDrivingLicenseUri(null);

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete5_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict(null);
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete6_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");

        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete7_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete8_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete9_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete10_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete11_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();

        mockProfile.setDob(null);


        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete12_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete13_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete14_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setDistrict("null");
        mockProfile.setWard("Phường Quang Trung");
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete15_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard(null);
        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete16_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("null");
        mockProfile.setHouseNumberStreet(null);
        mockProfile.setDrivingLicenseUri("test.jpg");

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete17_ShouldThrowException() {
        String accountId = "user123";


        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
        CreateBookingRequest.setCarId("car123");
        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        CreateBookingRequest.setPickUpTime(mockPickUpTime);
        CreateBookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();
        mockProfile.setFullName("test");
        mockProfile.setDob(LocalDate.of(2004, 11, 8));
        mockProfile.setNationalId("1234567890");
        mockProfile.setPhoneNumber("0886980035");
        mockProfile.setCityProvince("abc");
        mockProfile.setDistrict("null");
        mockProfile.setWard("null");
        mockProfile.setHouseNumberStreet("null");
        mockProfile.setDrivingLicenseUri(null);

        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(CreateBookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void updateStatusBookings_ShouldCancelExpiredBookings() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Booking expiredBooking = new Booking();
        expiredBooking.setCreatedAt(now.minusHours(2));
        expiredBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        when(bookingRepository.findExpiredBookings(any())).thenReturn(List.of(expiredBooking));

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.CANCELLED, expiredBooking.getStatus());
        verify(bookingRepository).saveAndFlush(expiredBooking);
    }

    @Test
    void updateStatusBookings_ShouldConfirmBookingIfWalletHasEnoughBalance() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Wallet wallet = new Wallet();
        wallet.setBalance(2000L);

        Account account = new Account();
        account.setId("testAccount");

        Car car = new Car();
        car.setId("1");

        Booking pendingBooking = new Booking();
        pendingBooking.setDeposit(1000L);
        pendingBooking.setAccount(account);
        pendingBooking.setCar(car);
        pendingBooking.setPickUpTime(now.plusHours(1));
        pendingBooking.setDropOffTime(now.plusHours(5));
        pendingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        when(bookingRepository.findPendingDepositBookings(any())).thenReturn(List.of(pendingBooking));
        when(walletRepository.findById(anyString())).thenReturn(Optional.of(wallet));
        when(bookingRepository.findByCarIdAndStatusAndTimeOverlap(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());


        doAnswer(invocation -> {
            String accId = invocation.getArgument(0);
            Long deposit = invocation.getArgument(1);
            Booking booking = invocation.getArgument(2);

            wallet.setBalance(wallet.getBalance() - deposit);
            booking.setStatus(EBookingStatus.WAITING_CONFIRMED);

            return null;
        }).when(transactionService).payDeposit(account.getId(), pendingBooking.getDeposit(), pendingBooking);

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRMED, pendingBooking.getStatus());
        assertEquals(1000L, wallet.getBalance());

        verify(transactionService).payDeposit(account.getId(), pendingBooking.getDeposit(), pendingBooking);
        verify(walletRepository).save(wallet);
        verify(bookingRepository).save(pendingBooking);
    }


    @Test
    void updateStatusBookings_ShouldCancelOverlappingBookings() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Wallet wallet = new Wallet();
        wallet.setBalance(2000L);
        Account account = new Account();
        account.setId("testAccount");

        Car car = new Car();
        car.setId("1");

        Booking confirmedBooking = new Booking();
        confirmedBooking.setDeposit(1000L);
        confirmedBooking.setAccount(account);
        confirmedBooking.setCar(car);
        confirmedBooking.setPickUpTime(now.plusHours(1));
        confirmedBooking.setDropOffTime(now.plusHours(5));
        confirmedBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        Booking overlappingBooking = new Booking();
        overlappingBooking.setCar(car);
        overlappingBooking.setPickUpTime(now.plusHours(2));
        overlappingBooking.setDropOffTime(now.plusHours(6));
        overlappingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        when(bookingRepository.findPendingDepositBookings(any())).thenReturn(List.of(confirmedBooking));
        when(walletRepository.findById(anyString())).thenReturn(Optional.of(wallet));
        when(bookingRepository.findByCarIdAndStatusAndTimeOverlap(any(), any(), any(), any()))
                .thenReturn(List.of(overlappingBooking));


        doAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setStatus(EBookingStatus.CANCELLED);
            return null;
        }).when(bookingRepository).saveAndFlush(overlappingBooking);

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRMED, confirmedBooking.getStatus());
        assertEquals(EBookingStatus.CANCELLED, overlappingBooking.getStatus());

        verify(bookingRepository).saveAndFlush(overlappingBooking);
    }


    @Test
    void updateStatusBookings_ShouldNotChangeStatusIfWalletBalanceIsNotEnough() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Wallet wallet = new Wallet();
        wallet.setBalance(500L);
        Account account = new Account();
        account.setId("testAccount");
        Booking pendingBooking = new Booking();
        pendingBooking.setDeposit(1000L);
        pendingBooking.setAccount(account);
        pendingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        when(bookingRepository.findPendingDepositBookings(any())).thenReturn(List.of(pendingBooking));
        when(walletRepository.findById(anyString())).thenReturn(Optional.of(wallet));

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.PENDING_DEPOSIT, pendingBooking.getStatus());
        verify(walletRepository, never()).save(wallet);
        verify(bookingRepository, never()).save(pendingBooking);
    }

    @Test
    void getWallet_Success() {

        String mockAccountId = "user123";
        when(SecurityUtil.getCurrentAccountId()).thenReturn(mockAccountId);


        Wallet mockWallet = new Wallet();
        mockWallet.setId(mockAccountId);
        mockWallet.setBalance(500000);

        when(walletRepository.findById(mockAccountId)).thenReturn(Optional.of(mockWallet));


        WalletResponse response = bookingService.getWallet();


        assertNotNull(response);
        assertEquals(mockAccountId, response.getId());
        assertEquals(500000, response.getBalance());
    }

    @Test
    void getWallet_AccountNotFound() {

        String mockAccountId = "user123";
        when(SecurityUtil.getCurrentAccountId()).thenReturn(mockAccountId);


        when(walletRepository.findById(mockAccountId)).thenReturn(Optional.empty());


        assertThrows(AppException.class, () -> bookingService.getWallet());
    }

    @Test
    void getBookingsOfCustomer_WithValidStatus_ReturnsBookingListResponse1() {
        // Given
        String accountId = "user123";
        String status = "CONFIRMED";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Booking booking = new Booking();
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setBasePrice(1000);
        booking.setCar(new Car());

        Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

        when(bookingRepository.findByAccountIdAndStatus(eq(accountId), eq(EBookingStatus.CONFIRMED), eq(pageable)))
                .thenReturn(bookingPage);
        when(bookingMapper.toBookingThumbnailResponse(any())).thenReturn(new BookingThumbnailResponse());

        // When
        BookingListResponse response = bookingService.getBookingsOfCustomer(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getBookings().getTotalElements());
    }


    @Test
    void confirmBooking_WithExpiredBooking_ThrowsException() {
        // Given
        String accountId = "user123";
        String bookingNumber = "BK123";
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        Role role = new Role();
        role.setName(ERole.CAR_OWNER);

        Car mockCar = new Car();
        mockCar.setAccount(mockAccount);

        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().minusDays(1));
        booking.setCar(mockCar);

        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(bookingRepository.findBookingByBookingNumber(eq(bookingNumber))).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.confirmBooking(bookingNumber));
        assertEquals(ErrorCode.BOOKING_EXPIRED, exception.getErrorCode());
        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
    }


    @Test
    void getBookingDetailsByBookingNumber_shouldThrowException_whenCarOwnerBookingNotFound() {
        // Given
        Account owner = new Account();
        owner.setId("user123");
        Role role = new Role();
        role.setName(ERole.CAR_OWNER);
        owner.setRole(role);

        when(SecurityUtil.getCurrentAccount()).thenReturn(owner);
        when(bookingRepository.findBookingByBookingNumberAndOwnerId("BK001", "user123")).thenReturn(null);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.getBookingDetailsByBookingNumber("BK001"));
        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void getBookingDetailsByBookingNumber_shouldThrowException_whenCustomerAccessesOthersBooking() {
        // Given
        Account customer = new Account();
        customer.setId("user123");
        Role role = new Role();
        role.setName(ERole.CUSTOMER);
        customer.setRole(role);

        Account anotherUser = new Account();
        anotherUser.setId("user456");

        Booking booking = new Booking();
        booking.setBookingNumber("BK001");
        booking.setAccount(anotherUser);

        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        when(bookingRepository.findBookingByBookingNumber("BK001")).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.getBookingDetailsByBookingNumber("BK001"));
        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }

    @Test
    void confirmBooking_shouldThrowException_whenBookingNotBelongToOwner() {
        // Given
        Account owner = new Account();
        owner.setId("user123");

        Account anotherOwner = new Account();
        anotherOwner.setId("user456");

        Car car = new Car();
        car.setAccount(anotherOwner);

        Booking booking = new Booking();
        booking.setBookingNumber("BK001");
        booking.setCar(car);

        when(SecurityUtil.getCurrentAccount()).thenReturn(owner);
        when(bookingRepository.findBookingByBookingNumber("BK001")).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.confirmBooking("BK001"));
        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }

    @Test
    void confirmBooking_shouldThrowException_whenBookingExpired() {
        // Given
        Account owner = new Account();
        owner.setId("user123");

        Car car = new Car();
        car.setAccount(owner);

        Booking booking = new Booking();
        booking.setBookingNumber("BK001");
        booking.setCar(car);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().minusDays(1));

        when(SecurityUtil.getCurrentAccount()).thenReturn(owner);
        when(bookingRepository.findBookingByBookingNumber("BK001")).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> bookingService.confirmBooking("BK001"));
        assertEquals(ErrorCode.BOOKING_EXPIRED, exception.getErrorCode());
        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void getBookingsOfCustomer_WithValidStatus_ReturnsBookingListResponse() {
        // Given
        String status = "CONFIRMED";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Booking booking = new Booking();
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setBasePrice(1000);
        booking.setCar(new Car());

        Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

        when(bookingRepository.findByAccountIdAndStatus(eq(accountId), eq(EBookingStatus.CONFIRMED), eq(pageable)))
                .thenReturn(bookingPage);
        when(bookingMapper.toBookingThumbnailResponse(any())).thenReturn(new BookingThumbnailResponse());

        // When
        BookingListResponse response = bookingService.getBookingsOfCustomer(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getBookings().getTotalElements());
        verify(bookingRepository).findByAccountIdAndStatus(eq(accountId), eq(EBookingStatus.CONFIRMED), eq(pageable));
    }

    @Test
    void getBookingsOfCustomer_WithInvalidStatus_ReturnsAllBookings() {
        // Given
        String status = "INVALID_STATUS"; // Trạng thái không hợp lệ
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Booking> bookingPage = new PageImpl<>(Collections.emptyList());

        when(bookingRepository.findByAccountId(eq(accountId), eq(pageable)))
                .thenReturn(bookingPage);

        // When
        BookingListResponse response = bookingService.getBookingsOfCustomer(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getBookings().getTotalElements());
        verify(bookingRepository).findByAccountId(eq(accountId), eq(pageable)); // Kiểm tra gọi đúng phương thức
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONFIRMED", "IN_PROGRESS", "CANCELLED"})
    void getBookingsOfCustomer_WithMultipleValidStatuses_ReturnsFilteredResults(String status) {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Booking> bookingPage = new PageImpl<>(Collections.emptyList());

        when(bookingRepository.findByAccountIdAndStatus(eq(accountId), eq(EBookingStatus.valueOf(status)), eq(pageable)))
                .thenReturn(bookingPage);

        // When
        BookingListResponse response = bookingService.getBookingsOfCustomer(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        verify(bookingRepository).findByAccountIdAndStatus(eq(accountId), eq(EBookingStatus.valueOf(status)), eq(pageable));
    }

    @Test
    void getBookingsOfCarOwner_WithInvalidStatus_ReturnsAllBookingsExceptPendingDeposit() {
        // Given
        String status = "INVALID_STATUS";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Booking> bookingPage = new PageImpl<>(Collections.emptyList());

        when(bookingRepository.findBookingsByCarOwnerId(eq(accountId), eq(EBookingStatus.PENDING_DEPOSIT), eq(pageable)))
                .thenReturn(bookingPage);

        // When
        BookingListResponse response = bookingService.getBookingsOfCarOwner(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getBookings().getTotalElements());
        verify(bookingRepository).findBookingsByCarOwnerId(eq(accountId), eq(EBookingStatus.PENDING_DEPOSIT), eq(pageable));
    }


    @Test
    void findBookingByBookingNumber_Success() {
        // Given
        String bookingNumber = "BK100";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);

        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // When
        Booking result = bookingRepository.findBookingByBookingNumber(bookingNumber);

        // Then
        assertNotNull(result);
        assertEquals(bookingNumber, result.getBookingNumber());
    }

    @Test
    void findBookingByBookingNumberAndOwnerId_Success() {
        // Given
        String bookingNumber = "BK200";
        String ownerId = "owner123";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);

        when(bookingRepository.findBookingByBookingNumberAndOwnerId(bookingNumber, ownerId)).thenReturn(booking);

        // When
        Booking result = bookingRepository.findBookingByBookingNumberAndOwnerId(bookingNumber, ownerId);

        // Then
        assertNotNull(result);
        assertEquals(bookingNumber, result.getBookingNumber());
    }

    @Test
    void existsByCarIdAndAccountIdAndBookingStatusIn_Success() {
        // Given
        String carId = "CAR001";
        String accountId = "user123";
        List<EBookingStatus> statuses = List.of(EBookingStatus.COMPLETED, EBookingStatus.PENDING_PAYMENT);

        when(bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(carId, accountId, statuses)).thenReturn(true);

        // When
        boolean result = bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(carId, accountId, statuses);

        // Then
        assertTrue(result);
    }


    @Test
    void confirmBooking_Success() {
        // Given
        String bookingNumber = "BK001";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1)); // Booking chưa hết hạn

        Car car = new Car();
        Account owner = new Account();
        owner.setId(accountId);
        car.setAccount(owner);
        booking.setCar(car);

        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // When
        BookingResponse response = bookingService.confirmBooking(bookingNumber);

        // Then
        assertNotNull(response);
        assertEquals(EBookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).saveAndFlush(booking);
    }

    @Test
    void confirmBooking_BookingNotFound_ShouldThrowException() {
        // Given
        String bookingNumber = "BK001";
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.confirmBooking(bookingNumber)
        );

        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void confirmBooking_NotCarOwner_ShouldThrowException() {
        // Given
        String bookingNumber = "BK001";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);

        Car car = new Car();
        Account owner = new Account();
        owner.setId("otherUser"); // Không phải chủ xe đang đăng nhập
        car.setAccount(owner);
        booking.setCar(car);

        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.confirmBooking(bookingNumber)
        );

        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }

    @Test
    void confirmBooking_InvalidStatus_ShouldThrowException() {
        // Given
        String bookingNumber = "BK001";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.IN_PROGRESS); // Trạng thái không hợp lệ

        Car car = new Car();
        Account owner = new Account();
        owner.setId(accountId);
        car.setAccount(owner);
        booking.setCar(car);

        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.confirmBooking(bookingNumber)
        );

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void confirmBooking_ExpiredBooking_ShouldThrowException() {
        // Given
        String bookingNumber = "BK001";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().minusHours(1)); // Quá hạn

        Car car = new Car();
        Account owner = new Account();
        owner.setId(accountId);
        car.setAccount(owner);
        booking.setCar(car);

        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.confirmBooking(bookingNumber)
        );

        assertEquals(ErrorCode.BOOKING_EXPIRED, exception.getErrorCode());
        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }
}