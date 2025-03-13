package com.mp.karental.service;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.booking.BookingRequest;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.entity.*;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.BookingMapper;
import com.mp.karental.repository.*;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.service.*;
import com.mp.karental.util.RedisUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void createBooking_Success() throws AppException {
        String accountId = "user123";

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);
        bookingRequest.setDriverFullName("test");
        bookingRequest.setDriverNationalId("1234567890");
        bookingRequest.setDriverPhoneNumber("0886980035");

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
        when(mockDrivingLicense.isEmpty()).thenReturn(false);
        when(fileService.getFileExtension(any(MultipartFile.class))).thenReturn(".jpg"); 
        bookingRequest.setDriverDrivingLicense(mockDrivingLicense);

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRM);
        booking.setBookingNumber("BK123");
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);

        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);
        when(bookingMapper.toBooking(any())).thenReturn(booking);
        when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());

        doAnswer(invocation -> {
            wallet.setBalance(wallet.getBalance() - car.getDeposit());
            return null;
        }).when(transactionService).payDeposit(accountId, car.getDeposit(), booking);


        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertNotNull(response);
        assertEquals(5000, wallet.getBalance());
        verify(transactionService, times(1)).payDeposit(accountId, car.getDeposit(), booking);
        verify(walletRepository, times(1)).save(wallet);
        verify(bookingRepository, times(1)).save(any());

        verify(fileService, times(1)).uploadFile(mockDrivingLicense, "booking/BK123/driver-driving-license.jpg");
    }

    @Test
    void createBooking_WhenDriverInfoInvalid_ShouldThrowAppException() {
        // Given
        String accountId = "user123";

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);
        bookingRequest.setDriverFullName("test");
        bookingRequest.setDriverNationalId("2134567890");
        bookingRequest.setDriverPhoneNumber("0886980035");

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

        Booking booking = new Booking();
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(5000);
        booking.setBasePrice(2000);
        booking.setStatus(EBookingStatus.WAITING_CONFIRM);
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

        
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.createBooking(bookingRequest));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO.name(), exception.getErrorCode().name());
    }




    @Test
    void createBooking_CarNotFound_ThrowsException() {
        BookingRequest request = new BookingRequest();
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
        BookingRequest request = new BookingRequest();
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
        BookingRequest request = new BookingRequest();
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

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("1");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        bookingRequest.setPickUpTime(pickUpTime);
        bookingRequest.setDropOffTime(dropOffTime);
        bookingRequest.setDriverFullName("Test User");
        bookingRequest.setDriverNationalId("1234567890");
        bookingRequest.setDriverPhoneNumber("0987654321");

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
        when(carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            BookingRequest request = invocation.getArgument(0);
            Booking mappedBooking = new Booking();
            mappedBooking.setPickUpTime(request.getPickUpTime());
            mappedBooking.setDropOffTime(request.getDropOffTime());
            mappedBooking.setPaymentType(request.getPaymentType());
            mappedBooking.setStatus(EBookingStatus.WAITING_CONFIRM);
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
        BookingResponse response = bookingService.createBooking(bookingRequest);

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, response.getStatus());
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

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("1");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        bookingRequest.setPickUpTime(pickUpTime);
        bookingRequest.setDropOffTime(dropOffTime);
        bookingRequest.setDriverNationalId("1234567890");
        bookingRequest.setDriverPhoneNumber("0987654321");
        bookingRequest.setDriverFullName("Test User");

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
        when(carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            BookingRequest request = invocation.getArgument(0);
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
        BookingResponse response = bookingService.createBooking(bookingRequest);

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

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("1");
        bookingRequest.setPaymentType(EPaymentType.CASH);
        bookingRequest.setPickUpTime(pickUpTime);
        bookingRequest.setDropOffTime(dropOffTime);
        bookingRequest.setDriverNationalId("1234567890");
        bookingRequest.setDriverPhoneNumber("0987654321");
        bookingRequest.setDriverFullName("Test User");


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
        when(carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            BookingRequest request = invocation.getArgument(0);
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
        BookingResponse response = bookingService.createBooking(bookingRequest);


        // Then
        assertEquals(EBookingStatus.PENDING_DEPOSIT, response.getStatus());
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());
    }

    @Test
    void createBooking_ProfileIncomplete_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete1_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete2_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete4_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete3_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete5_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete6_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete7_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete8_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete9_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete10_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete11_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();

        mockProfile.setDob(null);


        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete12_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete13_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete14_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete15_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete16_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete17_ShouldThrowException() {
        String accountId = "user123";


        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(bookingRequest);
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
            booking.setStatus(EBookingStatus.WAITING_CONFIRM);

            return null;
        }).when(transactionService).payDeposit(account.getId(), pendingBooking.getDeposit(), pendingBooking);

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, pendingBooking.getStatus());
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
        assertEquals(EBookingStatus.WAITING_CONFIRM, confirmedBooking.getStatus()); 
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
    void getBookingsByUserId_Success() {
        // Arrange
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        int page = 0;
        int size = 10;
        String sort = "createdAt,DESC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Booking booking = new Booking();
        booking.setPickUpTime(LocalDateTime.now());
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setBasePrice(100);


        Car car = new Car();
        car.setCarImageFront("car_front.jpg");
        booking.setCar(car);

        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking));

        BookingThumbnailResponse responseMock = new BookingThumbnailResponse();
        responseMock.setNumberOfDay(3);
        responseMock.setTotalPrice(300);

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(bookingsPage);
        when(bookingMapper.toBookingThumbnailResponse(any())).thenReturn(responseMock);
        when(fileService.getFileUrl(anyString())).thenReturn("test.jpg");

        // Act
        Page<BookingThumbnailResponse> result = bookingService.getBookingsByUserId(page, size, sort);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(3, result.getContent().get(0).getNumberOfDay());
        assertEquals(300, result.getContent().get(0).getTotalPrice());

        verify(bookingRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));

    }


    @Test
    void getBookingsByUserId_EmptyResult() {

        int page = 0;
        int size = 10;
        String sort = "createdAt,DESC";

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        Page<BookingThumbnailResponse> result = bookingService.getBookingsByUserId(page, size, sort);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(bookingRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }

    @Test
    void getBookingsByUserId_InvalidPage_ShouldResetToZero() {
        // Arrange
        int page = -1;

        int size = 10;
        String sort = "createdAt,DESC";

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);


        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getPageNumber() == 0
        ));
    }

    @Test
    void getBookingsByUserId_InvalidSize_ShouldResetToDefault() {
        // Arrange
        int page = 0;
        int size = 200; 
        String sort = "createdAt,DESC";

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);


        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getPageSize() == 10
        ));
    }

    @Test
    void getBookingsByUserId_InvalidSort_ShouldUseDefault() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "invalidSort,DESC"; 

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);


        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getSort().equals(Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @Test
    void getBookingsByUserId_SortByBasePrice() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "basePrice,ASC"; 

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);


        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getSort().equals(Sort.by(Sort.Direction.ASC, "basePrice"))
        ));
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

}
