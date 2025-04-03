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
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    private AccountRepository accountRepository;

    @Mock
    private CarService carService;
    @Mock
    private EmailService emailService;

    @Mock
    private MultipartFile mockFile;

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
    void testPayDepositAgain_InvalidPaymentType() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");
        booking.setPaymentType(EPaymentType.BANK_TRANSFER);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.payDepositAgain(bookingNumber);
        });

        assertEquals(ErrorCode.UNSUPPORTED_PAYMENT_TYPE, exception.getErrorCode());
    }

    @Test
    void testPayDepositAgain_InvalidStatus() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");
        booking.setPaymentType(EPaymentType.WALLET);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.payDepositAgain(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testPayTotalPaymentAgain_InvalidStatus() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");
        booking.setPaymentType(EPaymentType.WALLET);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.payTotalPaymentAgain(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void getBookingsOfOperator_ReturnsAllBookings() {
        // Given
        String status = "INVALID_STATUS";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Booking> bookingPage = new PageImpl<>(Collections.emptyList());
        List<EPaymentType> bankCashTypes = Arrays.asList(EPaymentType.BANK_TRANSFER, EPaymentType.CASH);
        when(bookingRepository.findAllBookings(eq(pageable))).thenReturn(bookingPage);

        // When
        BookingListResponse response = bookingService.getBookingsOfOperator(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getBookings().getTotalElements());
        verify(bookingRepository, times(1)).findAllBookings(eq(pageable));
    }

    @Test
    void testPayTotalPaymentAgain_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.PENDING_PAYMENT);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");
        booking.setPaymentType(EPaymentType.WALLET);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.payTotalPaymentAgain(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void testPayDepositAgain_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");
        booking.setPaymentType(EPaymentType.WALLET);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.payDepositAgain(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.WAITING_CONFIRMED, booking.getStatus());
    }

    @Test
    void testRejectDeposit_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("cus123");
        customer.setEmail("cus@gmail");

        Account operator = new Account();
        operator.setId("user123");
        operator.setEmail("operator@example.com");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(operator);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.rejectDeposit(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository, times(1)).saveAndFlush(booking);
    }

    @Test
    void testConfirmDeposit_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("cus123");
        customer.setEmail("cus@gmail");

        Account operator = new Account();
        operator.setId("user123");
        operator.setEmail("operator@example.com");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(operator);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.confirmDeposit(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.WAITING_CONFIRMED, booking.getStatus());
        verify(bookingRepository, times(1)).saveAndFlush(booking);
    }

    @Test
    void testConfirmBooking_Expired() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().minusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_EXPIRED, exception.getErrorCode());
    }

    @Test
    void testConfirmBooking_ExpiredNull() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(null);
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_EXPIRED, exception.getErrorCode());
    }

    @Test
    void testProcessOverdueWaitingConfirmReturnCarBookings() {
        // Giả lập thời gian hiện tại, làm tròn đến phút
        LocalDateTime now = LocalDateTime.now();
        Account account = new Account();
        account.setId(accountId);
        account.setEmail("customer@mail.com");

        Account owner = new Account();
        owner.setId(accountId);
        owner.setEmail("owner@mail.com");
        Car car = new Car();
        car.setId("123");
        car.setAccount(owner);

        Car car2 = new Car();
        car.setId("1234");
        car.setAccount(owner);
        // Tạo danh sách booking quá hạn
        Booking booking1 = new Booking();
        booking1.setBookingNumber("1");
        booking1.setDropOffTime(now.minusMinutes(5)); // 5 phút trước
        booking1.setStatus(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR);
        booking1.setAccount(account);
        booking1.setCar(car);

        Booking booking2 = new Booking();
        booking2.setBookingNumber("2");
        booking2.setDropOffTime(now.minusMinutes(10)); // 10 phút trước
        booking2.setStatus(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR);
        booking2.setAccount(account);
        booking2.setCar(car2);

        List<Booking> overdueBookings = Arrays.asList(booking1, booking2);

        // Mock repository với cùng thời gian đã làm tròn
        lenient().when(bookingRepository.findOverdueDropOffs(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR, now))
                .thenReturn(overdueBookings);

        lenient().when(bookingRepository.bulkUpdateWaitingConfirmedReturnCarStatus(EBookingStatus.IN_PROGRESS,
                        EBookingStatus.WAITING_CONFIRMED_RETURN_CAR, now.minusMinutes(1)))
                .thenReturn(2);

        // Gọi method cần test
        bookingService.processOverdueWaitingBookings();


        // Kiểm tra email có được gửi không
        lenient().doAnswer(invocation -> {
            System.out.println("Email sent: " + Arrays.toString(invocation.getArguments()));
            return null;
        }).when(emailService).sendEarlyReturnRejectedEmail(anyString(), anyString());

        // Kiểm tra repository có được gọi đúng không
        verify(bookingRepository, times(1)).findOverdueDropOffs(eq(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR), any());
        verify(bookingRepository, times(1)).bulkUpdateWaitingConfirmedReturnCarStatus(eq(EBookingStatus.IN_PROGRESS),
                eq(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR), any());
    }

    @Test
    void testProcessOverdueWaitingConfirmBookings() {
        // Giả lập thời gian hiện tại, làm tròn đến phút
        LocalDateTime now = LocalDateTime.now();
        Account account = new Account();
        account.setId(accountId);
        account.setEmail("customer@mail.com");
        Account owner = new Account();
        owner.setId(accountId);
        owner.setEmail("owner@mail.com");
        Car car = new Car();
        car.setId("123");
        car.setAccount(owner);

        Car car2 = new Car();
        car.setId("1234");
        car.setAccount(owner);
        // Tạo danh sách booking quá hạn
        Booking booking1 = new Booking();
        booking1.setBookingNumber("1");
        booking1.setPickUpTime(now.minusMinutes(5)); // 5 phút trước
        booking1.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking1.setAccount(account);
        booking1.setCar(car);

        Booking booking2 = new Booking();
        booking2.setBookingNumber("2");
        booking2.setPickUpTime(now.minusMinutes(10)); // 10 phút trước
        booking2.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking2.setAccount(account);
        booking2.setCar(car2);

        List<Booking> overdueBookings = Arrays.asList(booking1, booking2);

        // Mock repository với cùng thời gian đã làm tròn
        lenient().when(bookingRepository.findOverduePickups(EBookingStatus.WAITING_CONFIRMED, now))
                .thenReturn(overdueBookings);

        lenient().when(bookingRepository.bulkUpdateWaitingConfirmedStatus(EBookingStatus.CANCELLED,
                        EBookingStatus.WAITING_CONFIRMED, now.minusMinutes(1)))
                .thenReturn(2);

        // Gọi method cần test
        bookingService.processOverdueWaitingBookings();

        // Kiểm tra refund có được gọi không
        lenient().doAnswer(invocation -> {
            System.out.println("Email sent: " + Arrays.toString(invocation.getArguments()));
            return null;
        }).when(transactionService).refundAllDeposit(any(Booking.class));

        // Kiểm tra email có được gửi không
        lenient().doAnswer(invocation -> {
            System.out.println("Email sent: " + Arrays.toString(invocation.getArguments()));
            return null;
        }).when(emailService).sendCancelledBookingEmail(anyString(), anyString(), anyString());

        // Kiểm tra repository có được gọi đúng không
        verify(bookingRepository, times(1)).findOverduePickups(eq(EBookingStatus.WAITING_CONFIRMED), any());
        verify(bookingRepository, times(1)).bulkUpdateWaitingConfirmedStatus(eq(EBookingStatus.CANCELLED),
                eq(EBookingStatus.WAITING_CONFIRMED), any());
    }

    @Test
    void testConfirmBooking_InvalidStatus() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(null);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmBooking(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testReturnCarEarly_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.IN_PROGRESS);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.returnCar(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR, booking.getStatus());
        verify(emailService, times(1)).sendWaitingConfirmReturnCarEmail("owner@example.com", bookingNumber);
    }

    @Test
    void testConfirmReturn_InvalidStatus() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(null);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmEarlyReturnCar(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testRejectReturn_InvalidStatus() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(null);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.rejectWaitingConfirmedEarlyReturnCarBooking(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testRejectBooking_InvalidStatus() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(null);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.rejectWaitingConfirmedBooking(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testRejectBooking_WhenCarInvalid() {
        // Arrange
        Booking booking = new Booking();
        String bookingNumber = "B123";
        booking.setBookingNumber(bookingNumber);
        Account account = new Account();
        account.setId("123");
        Account account1 = new Account();
        account1.setId("456");
        Car car = new Car();
        car.setId("12");
        car.setAccount(account1);
        booking.setCar(car);

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.rejectWaitingConfirmedBooking(bookingNumber);
        });

        assertEquals(ErrorCode.FORBIDDEN_CAR_ACCESS, exception.getErrorCode());
    }

    @Test
    void testConfirmReturn_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.confirmEarlyReturnCar(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.COMPLETED, booking.getStatus());
        verify(bookingRepository, times(1)).saveAndFlush(booking);
    }

    @Test
    void testRejectReturn_WhenBookingNotFound() {
        // Arrange
        String bookingNumber = "B123";
        Account account = new Account();
        account.setId("123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.rejectWaitingConfirmedEarlyReturnCarBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void testRejectReturn_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED_RETURN_CAR);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.rejectWaitingConfirmedEarlyReturnCarBooking(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.IN_PROGRESS, booking.getStatus());
        verify(emailService, times(1)).sendEarlyReturnRejectedEmail(
                eq("customer@example.com"),
                eq(bookingNumber)
        );
        verify(bookingRepository, times(1)).saveAndFlush(booking);
    }

    @Test
    void testRejectBooking_WhenBookingNotFound() {
        // Arrange
        String bookingNumber = "B123";
        Account account = new Account();
        account.setId("123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.rejectWaitingConfirmedBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void testRejectBooking_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.rejectWaitingConfirmedBooking(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
        verify(emailService, times(1)).sendCancelledBookingEmail(
                eq("customer@example.com"),
                eq("Toyota Camry"),
                eq("This booking was declined by car owner")
        );
        verify(bookingRepository, times(1)).saveAndFlush(booking);
    }

    @Test
    void testConfirmBooking_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(2));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        carOwner.setId("user1234");
        car.setAccount(carOwner);
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user1234");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.confirmBooking(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.CONFIRMED, booking.getStatus());
        verify(emailService, times(1)).sendConfirmBookingEmail(
                eq("customer@example.com"),
                eq("Toyota Camry"),
                eq(bookingNumber)
        );
        verify(bookingRepository, times(1)).saveAndFlush(booking);
    }

    @Test
    void testReturnCar_InsufficientBalance_ShouldMarkPendingPayment() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.IN_PROGRESS);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().minusDays(1));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");
        booking.setBasePrice(5000);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        when(SecurityUtil.getCurrentEmail()).thenReturn("customer@example.com");
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(1000);
        mockResponse.setBasePrice(5000);
        mockResponse.setTotalPrice(10000);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        when(bookingMapper.toBookingResponse(booking)).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.returnCar(bookingNumber);
        long remaining = mockResponse.getDeposit() - mockResponse.getTotalPrice();
        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.PENDING_PAYMENT, booking.getStatus());
        verify(emailService, times(1)).sendPendingPaymentEmail(booking.getAccount().getEmail(), bookingNumber, -remaining);
    }
    @ParameterizedTest
    @CsvSource({
            "PENDING_DEPOSIT", //booked but hasn't paid deposit yet
            "WAITING_CONFIRMED",
            "CONFIRMED", //paid deposit
            "CANCELLED", //cancelled booking
            "PENDING_PAYMENT", //customer returned car but hasn't complete payment
            "COMPLETED" //returned car and completed payment
    })
    void testReturnCar_InvalidStatus_ShouldThrowException(EBookingStatus status) {
        
        Account account = new Account();
        account.setId(accountId);
        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber("BK123");
        existingBooking.setAccount(account);
        existingBooking.setCar(car);
        existingBooking.setStatus(status);

        when(bookingRepository.findBookingByBookingNumber("BK123")).thenReturn(existingBooking);
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(account);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> bookingService.returnCar("BK123"));
        assertEquals(ErrorCode.CAR_CANNOT_RETURN, exception.getErrorCode());
    }

    @Test
    void testCancelBooking_Confirmed_ShouldRefundPartialDeposit() {
        // Arrange
        Account account = new Account();
        account.setId(accountId);
        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber("BK123");
        existingBooking.setAccount(account);
        existingBooking.setCar(car);
        existingBooking.setStatus(EBookingStatus.CONFIRMED);
        existingBooking.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0));
        existingBooking.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0));
        existingBooking.setDriverDrivingLicenseUri("abc");


        when(bookingRepository.findBookingByBookingNumber("BK123")).thenReturn(existingBooking);
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(account);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.cancelBooking("BK123");

        // Assert
        verify(transactionService, times(1)).refundPartialDeposit(existingBooking); 
        verify(bookingRepository, times(1)).saveAndFlush(existingBooking); 
        assertEquals(EBookingStatus.CANCELLED, existingBooking.getStatus()); 
    }

    @Test
    void testCancelBooking_WaitingConfirmed_ShouldRefundFullDeposit() {
        // Arrange
        Account account = new Account();
        account.setId(accountId);
        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber("BK123");
        existingBooking.setAccount(account);
        existingBooking.setCar(car);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        existingBooking.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0));
        existingBooking.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0));
        existingBooking.setDriverDrivingLicenseUri("abc");


        when(bookingRepository.findBookingByBookingNumber("BK123")).thenReturn(existingBooking);
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(account);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.cancelBooking("BK123");

        // Assert
        verify(transactionService, times(1)).refundAllDeposit(existingBooking); 
        verify(bookingRepository, times(1)).saveAndFlush(existingBooking); 
        assertEquals(EBookingStatus.CANCELLED, existingBooking.getStatus()); 
    }

    @Test
    void testCancelBooking_PendingDeposit_ShouldRefundFullDeposit() {
        // Arrange
        Account account = new Account();
        account.setId(accountId);
        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber("BK123");
        existingBooking.setAccount(account);
        existingBooking.setCar(car);
        existingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        existingBooking.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0));
        existingBooking.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0));
        existingBooking.setDriverDrivingLicenseUri("abc");


        when(bookingRepository.findBookingByBookingNumber("BK123")).thenReturn(existingBooking);
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(account);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.cancelBooking("BK123");

        // Assert
        verify(bookingRepository, times(1)).saveAndFlush(existingBooking);
        assertEquals(EBookingStatus.CANCELLED, existingBooking.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "IN_PROGRESS",
            "COMPLETED",
            "CANCELLED",
            "PENDING_PAYMENT",
            "WAITING_CONFIRMED_RETURN_CAR"
    })
    void testCancelBooking_InvalidStatus_ShouldThrowException(EBookingStatus status) {
        
        Account account = new Account();
        account.setId(accountId);
        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber("BK123");
        existingBooking.setAccount(account);
        existingBooking.setCar(car);
        existingBooking.setStatus(status);

        when(bookingRepository.findBookingByBookingNumber("BK123")).thenReturn(existingBooking);
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(account);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> bookingService.cancelBooking("BK123"));
        assertEquals(ErrorCode.BOOKING_CANNOT_CANCEL, exception.getErrorCode());
    }
    @Test
    void editBooking_NationalIdException() throws AppException {
        MultipartFile drivingLicense = mock(MultipartFile.class);
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverDrivingLicense(drivingLicense);

        request.setDriverDob(LocalDate.now().minusYears(18));
        request.setDriverCityProvince("abc");
        request.setDriverDistrict("abc");
        request.setDriverWard("abc");
        request.setDriverPhoneNumber("0886980035");
        request.setDriverFullName("abc");
        request.setDriverNationalId("12390");
        request.setDriverHouseNumberStreet("abc");
        request.setDriverEmail("abc@gmail.com");
        request.setDriver(true);

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

        AppException exception = assertThrows(AppException.class, () -> bookingService.editBooking(request,bookingNumber));
        assertEquals(ErrorCode.INVALID_NATIONAL_ID, exception.getErrorCode());

    }

    @Test
    void editBooking_PhoneNumberException() throws AppException {
        MultipartFile drivingLicense = mock(MultipartFile.class);
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverDrivingLicense(drivingLicense);

        request.setDriverDob(LocalDate.now().minusYears(18));
        request.setDriverCityProvince("abc");
        request.setDriverDistrict("abc");
        request.setDriverWard("abc");
        request.setDriverPhoneNumber("0886980");
        request.setDriverFullName("abc");
        request.setDriverNationalId("1234567890");
        request.setDriverHouseNumberStreet("abc");
        request.setDriverEmail("abc@gmail.com");
        request.setDriver(true);

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

        AppException exception = assertThrows(AppException.class, () -> bookingService.editBooking(request,bookingNumber));
        assertEquals(ErrorCode.INVALID_PHONE_NUMBER, exception.getErrorCode());

    }
    @Test
    void testCreateBooking_InvalidNationalId_ShouldThrowException() {
        // Arrange
        MultipartFile drivingLicense = mock(MultipartFile.class); // Mock MultipartFile

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setDriver(true);
        request.setDriverDrivingLicense(drivingLicense);

        request.setDriverDob(LocalDate.now().minusYears(18));
        request.setDriverCityProvince("abc");
        request.setDriverDistrict("abc");
        request.setDriverWard("abc");
        request.setDriverPhoneNumber("0886980035");
        request.setDriverFullName("abc");
        request.setDriverNationalId("1234890");
        request.setDriverHouseNumberStreet("abc");
        request.setDriverEmail("abc@gmail.com");

        Account customer = new Account();
        customer.setId("user123");
        UserProfile profile = new UserProfile();
        profile.setDrivingLicenseUri("existing-license-uri");
        profile.setDob(LocalDate.now().minusYears(18));
        profile.setCityProvince("abc");
        profile.setDistrict("abc");
        profile.setWard("abc");
        profile.setPhoneNumber("0886980035");
        profile.setFullName("abc");
        profile.setNationalId("1234567890");
        profile.setHouseNumberStreet("abc");

        customer.setProfile(profile);
        profile.setAccount(customer);
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);

        Wallet wallet = new Wallet();
        wallet.setBalance(1000);

        Car car = new Car();
        car.setId("car123");
        car.setDeposit(500);
        car.setBasePrice(100);
        car.setAccount(customer);

        Booking booking = new Booking();
        booking.setBookingNumber("BK12345");
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        lenient().when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        lenient().when(carService.isCarAvailable(any(), any(), any())).thenReturn(true);
        lenient().when(bookingMapper.toBooking(request)).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK12345");
        lenient().when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Mock file handling
        lenient().when(drivingLicense.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(drivingLicense)).thenReturn(".png");

        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));
        assertEquals(ErrorCode.INVALID_NATIONAL_ID, exception.getErrorCode());
    }

    @Test
    void testCreateBooking_InvalidPhoneNumber_ShouldThrowException() {
        // Arrange
        MultipartFile drivingLicense = mock(MultipartFile.class); // Mock MultipartFile

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setDriver(true);
        request.setDriverDrivingLicense(drivingLicense);

        request.setDriverDob(LocalDate.now().minusYears(18));
        request.setDriverCityProvince("abc");
        request.setDriverDistrict("abc");
        request.setDriverWard("abc");
        request.setDriverPhoneNumber("886980035");
        request.setDriverFullName("abc");
        request.setDriverNationalId("1234567890");
        request.setDriverHouseNumberStreet("abc");
        request.setDriverEmail("abc@gmail.com");

        Account customer = new Account();
        customer.setId("user123");
        UserProfile profile = new UserProfile();
        profile.setDrivingLicenseUri("existing-license-uri");
        profile.setDob(LocalDate.now().minusYears(18));
        profile.setCityProvince("abc");
        profile.setDistrict("abc");
        profile.setWard("abc");
        profile.setPhoneNumber("0886980035");
        profile.setFullName("abc");
        profile.setNationalId("1234567890");
        profile.setHouseNumberStreet("abc");

        customer.setProfile(profile);
        profile.setAccount(customer);
        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);

        Wallet wallet = new Wallet();
        wallet.setBalance(1000);

        Car car = new Car();
        car.setId("car123");
        car.setDeposit(500);
        car.setBasePrice(100);
        car.setAccount(customer);

        Booking booking = new Booking();
        booking.setBookingNumber("BK12345");
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setCar(car);

        lenient().when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        lenient().when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        lenient().when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        lenient().when(carService.isCarAvailable(any(), any(), any())).thenReturn(true);
        lenient().when(bookingMapper.toBooking(request)).thenReturn(booking);
        lenient().when(redisUtil.generateBookingNumber()).thenReturn("BK12345");
        lenient().when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Mock file handling
        lenient().when(drivingLicense.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(drivingLicense)).thenReturn(".png");

        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));
        assertEquals(ErrorCode.INVALID_PHONE_NUMBER, exception.getErrorCode());
    }

    @Test
    void testCreateBooking_Success_WithOverlappingBookings() throws AppException {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(3));
        request.setPaymentType(EPaymentType.WALLET);
        request.setDriver(false);

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        UserProfile profile = new UserProfile();
        profile.setDrivingLicenseUri("existing-license-uri");
        profile.setDob(LocalDate.now().minusYears(18));
        profile.setCityProvince("abc");
        profile.setDistrict("abc");
        profile.setWard("abc");
        profile.setPhoneNumber("0886980035");
        profile.setFullName("abc");
        profile.setNationalId("1234567890");
        profile.setHouseNumberStreet("abc");
        customer.setProfile(profile);
        profile.setAccount(customer);

        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);

        Car car = new Car();
        car.setId("123");
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setDeposit(500);
        car.setBasePrice(100);
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);

        Wallet wallet = new Wallet();
        wallet.setBalance(1000);

        Booking booking = new Booking();
        booking.setBookingNumber("BK12345");
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(500);
        booking.setBasePrice(100);
        booking.setPickUpTime(request.getPickUpTime());
        booking.setDropOffTime(request.getDropOffTime());
        booking.setCar(car);
        booking.setAccount(customer);

        Booking overlappingBooking = new Booking();
        overlappingBooking.setBookingNumber("BK67890");
        overlappingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        overlappingBooking.setPickUpTime(LocalDateTime.now().plusDays(2));
        overlappingBooking.setDropOffTime(LocalDateTime.now().plusDays(4));
        overlappingBooking.setCar(car);
        overlappingBooking.setAccount(new Account());
        overlappingBooking.getAccount().setEmail("overlap@example.com");

        when(carRepository.findById("123")).thenReturn(Optional.of(car));
        when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable("123", request.getPickUpTime(), request.getDropOffTime())).thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("BK12345");
        when(bookingMapper.toBooking(request)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        when(bookingRepository.findByCarIdAndStatusAndTimeOverlap(
                eq("123"), eq(EBookingStatus.PENDING_DEPOSIT),
                any(LocalDateTime.class), any(LocalDateTime.class))
        ).thenReturn(List.of(overlappingBooking));

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");

        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);

        // Act
        BookingResponse response = bookingService.createBooking(request);

        // Assert
        assertNotNull(response);
        assertEquals("BK12345", response.getBookingNumber());
        assertEquals(500.0, response.getDeposit());
        assertEquals(100.0, response.getBasePrice());

        
        assertEquals(EBookingStatus.CANCELLED, overlappingBooking.getStatus());

        
        verify(redisUtil, times(1)).generateBookingNumber();
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(redisUtil, never()).cachePendingDepositBooking(anyString());
        verify(bookingRepository, times(1)).saveAndFlush(overlappingBooking);
        verify(emailService, times(1)).sendCancelledBookingEmail(
                eq("overlap@example.com"),
                eq("Toyota Corolla"),
                contains("canceled because another customer has successfully placed a deposit")
        );

        verify(redisUtil, times(1)).removeCachePendingDepositBooking("BK12345");

        verify(emailService, times(1)).sendWaitingConfirmedEmail(
                eq("customer@example.com"),
                eq("owner@example.com"),
                eq("Toyota Corolla"),
                eq("BK12345")
        );
    }

    @Test
    void testCreateBooking_Success_WithDriverLicense() throws AppException {
        // Arrange
        MultipartFile drivingLicense = mock(MultipartFile.class); // Mock MultipartFile

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setDriver(true);
        request.setDriverDrivingLicense(drivingLicense);

        request.setDriverDob(LocalDate.now().minusYears(18));
        request.setDriverCityProvince("abc");
        request.setDriverDistrict("abc");
        request.setDriverWard("abc");
        request.setDriverPhoneNumber("0886980035");
        request.setDriverFullName("abc");
        request.setDriverNationalId("1234567890");
        request.setDriverHouseNumberStreet("abc");
        request.setDriverEmail("abc@gmail.com");

        Account customer = new Account();
        customer.setId("user123");
        UserProfile profile = new UserProfile();
        profile.setDrivingLicenseUri("existing-license-uri");
        profile.setDob(LocalDate.now().minusYears(18));
        profile.setCityProvince("abc");
        profile.setDistrict("abc");
        profile.setWard("abc");
        profile.setPhoneNumber("0886980035");
        profile.setFullName("abc");
        profile.setNationalId("1234567890");
        profile.setHouseNumberStreet("abc");

        customer.setProfile(profile);
        profile.setAccount(customer);
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);

        Wallet wallet = new Wallet();
        wallet.setBalance(1000);

        Car car = new Car();
        car.setId("car123");
        car.setDeposit(500);
        car.setBasePrice(100);
        car.setAccount(customer);

        Booking booking = new Booking();
        booking.setBookingNumber("BK12345");
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setCar(car);

        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(any(), any(), any())).thenReturn(true);
        when(bookingMapper.toBooking(request)).thenReturn(booking);
        when(redisUtil.generateBookingNumber()).thenReturn("BK12345");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Mock file handling
        when(drivingLicense.isEmpty()).thenReturn(false);
        when(fileService.getFileExtension(drivingLicense)).thenReturn(".png");

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");

        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);

        // Act
        BookingResponse response = bookingService.createBooking(request);

        // Assert
        assertNotNull(response);
        assertEquals("BK12345", response.getBookingNumber());

        // Verify file upload call
        verify(fileService).uploadFile(drivingLicense, "booking/BK12345/driver-driving-license.png");

        // Ensure booking is saved
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }


    @Test
    void testReturnCar_Success() {
        // Arrange
        String bookingNumber = "BK12345";
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(EBookingStatus.IN_PROGRESS);
        booking.setPickUpTime(LocalDateTime.now().minusDays(2));
        booking.setDropOffTime(LocalDateTime.now().minusDays(1));
        booking.setDeposit(1000);
        booking.setDriverDrivingLicenseUri("existing-license-uri");

        Account customer = new Account();
        customer.setId("user123");
        customer.setEmail("customer@example.com");
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);
        booking.setAccount(customer);

        Car car = new Car();
        Account carOwner = new Account();
        carOwner.setEmail("owner@example.com");
        car.setAccount(carOwner);
        booking.setCar(car);

        Wallet wallet = new Wallet();
        wallet.setBalance(500);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);
        // Act
        BookingResponse response = bookingService.returnCar(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.COMPLETED, booking.getStatus());
        verify(transactionService, times(1)).offsetFinalPayment(booking);
        verify(emailService, times(1)).sendPaymentEmailToCarOwner("owner@example.com", bookingNumber, (long) (0.92 * response.getTotalPrice()));
    }

    @Test
    void testCreateBooking_Success_WithWalletPayment() throws AppException {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(3));
        request.setPaymentType(EPaymentType.WALLET);
        request.setDriver(false); // No separate driver

        Account customer = new Account();
        customer.setId("user123");
        UserProfile profile = new UserProfile();
        profile.setDrivingLicenseUri("existing-license-uri");
        profile.setDob(LocalDate.now().minusYears(18));
        profile.setCityProvince("abc");
        profile.setDistrict("abc");
        profile.setWard("abc");
        profile.setPhoneNumber("0886980035");
        profile.setFullName("abc");
        profile.setNationalId("1234567890");
        profile.setHouseNumberStreet("abc");

        customer.setProfile(profile);
        profile.setAccount(customer);
        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(customer);

        Car car = new Car();
        car.setId("123");
        car.setDeposit(500);
        car.setBasePrice(100);
        car.setAccount(customer);

        Wallet wallet = new Wallet();
        wallet.setBalance(1000);

        Booking booking = new Booking();
        booking.setBookingNumber("BK12345");
        booking.setPaymentType(EPaymentType.WALLET);
        booking.setDeposit(500);
        booking.setBasePrice(100);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));

        when(carRepository.findById("123")).thenReturn(Optional.of(car));
        when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable("123", request.getPickUpTime(), request.getDropOffTime())).thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("BK12345");
        when(bookingMapper.toBooking(request)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingNumber("BK12345");
        mockResponse.setDeposit(500);
        mockResponse.setBasePrice(100);
        mockResponse.setDriverDrivingLicenseUrl("existing-license-uri");

        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(mockResponse);

        // Act
        BookingResponse response = bookingService.createBooking(request);
        // Assert
        assertNotNull(response);
        assertEquals("BK12345", response.getBookingNumber());
        assertEquals(500.0, response.getDeposit());
        assertEquals(100.0, response.getBasePrice());

        verify(redisUtil, times(1)).generateBookingNumber();
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(redisUtil, never()).cachePendingDepositBooking(anyString()); // Wallet has enough balance
    }

    @Test
    void testConfirmBooking_WhenBookingNotFound() {
        // Arrange
        String bookingNumber = "B123";
        Account account = new Account();
        account.setId("123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null); 

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());
    }


    @Test
    void testGetBookingDetailsByBookingNumber_Success() {
        // Arrange
        String bookingNumber = "B123";
        String userId = "123";

        
        Account customerAccount = new Account();
        customerAccount.setId(userId);
        Role customerRole = new Role();
        customerRole.setName(ERole.CUSTOMER);
        customerAccount.setRole(customerRole);
        Car car = new Car();
        car.setId("123");
        
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(customerAccount);
        booking.setDriverDrivingLicenseUri("user/driver-license.jpg");
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        booking.setCar(car);

        // Mock SecurityUtil
        when(SecurityUtil.getCurrentAccountId()).thenReturn(userId);
        when(SecurityUtil.getCurrentAccount()).thenReturn(customerAccount);

        // Mock repository
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // Act
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setDriverDrivingLicenseUrl("mock-url");
        mockResponse.setBookingNumber(bookingNumber);

        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);

        
        BookingResponse response = bookingService.getBookingDetailsByBookingNumber(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(bookingNumber, response.getBookingNumber());
    }

    @Test
    void testGetBookingDetailsByBookingNumber_UnauthorizedAccess() {
        // Arrange
        String bookingNumber = "B123";
        String userId = "999"; 

        
        Account unauthorizedAccount = new Account();
        unauthorizedAccount.setId(userId);
        Role unauthorizedRole = new Role();
        unauthorizedRole.setName(ERole.ADMIN); 
        unauthorizedAccount.setRole(unauthorizedRole);

        
        when(SecurityUtil.getCurrentAccountId()).thenReturn(userId);
        when(SecurityUtil.getCurrentAccount()).thenReturn(unauthorizedAccount);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.getBookingDetailsByBookingNumber(bookingNumber);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void testGetBookingDetailsByBookingNumber_ForbiddenCarAccess() {
        // Arrange
        String bookingNumber = "B123";
        String ownerId = "123"; 
        String wrongOwnerId = "999"; 

        
        Account carOwnerAccount = new Account();
        carOwnerAccount.setId(wrongOwnerId); 
        Role carOwnerRole = new Role();
        carOwnerRole.setName(ERole.CAR_OWNER);
        carOwnerAccount.setRole(carOwnerRole);

        
        Account actualCarOwner = new Account();
        actualCarOwner.setId(ownerId);
        Car car = new Car();
        car.setId("CAR123");
        car.setAccount(actualCarOwner);

        
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setCar(car);

        
        when(SecurityUtil.getCurrentAccount()).thenReturn(carOwnerAccount);


        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.getBookingDetailsByBookingNumber(bookingNumber);
        });

        assertEquals(ErrorCode.FORBIDDEN_CAR_ACCESS, exception.getErrorCode());
    }

    @Test
    void testConfirmPickUp_BookingCannotBePickedUp_NotConfirmed() {
        // Arrange
        String bookingNumber = "B123";

        Account account = new Account();
        account.setId("123");

        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(account);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.CANCELLED); 
        booking.setPickUpTime(LocalDateTime.now().plusMinutes(30));
        booking.setDropOffTime(LocalDateTime.now().plusDays(1));

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmPickUp(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_CANNOT_PICKUP, exception.getErrorCode());
    }

    @Test
    void testConfirmPickUp_ForbiddenAccess() {
        // Arrange
        String bookingNumber = "B123";

        Account account1 = new Account();
        account1.setId("123"); 

        Account account2 = new Account();
        account2.setId("999"); 

        Car car = new Car();
        car.setId("123");
        car.setAccount(account1); 

        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(account1); 
        booking.setCar(car);
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusMinutes(30));
        booking.setDropOffTime(LocalDateTime.now().plusDays(1));
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("999"); 
        when(SecurityUtil.getCurrentAccount()).thenReturn(account2);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmPickUp(bookingNumber);
        });

        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }

    @Test
    void testConfirmPickUp_BookingNotFound() {
        // Arrange
        String bookingNumber = "B123";
        Account account = new Account();
        account.setId("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null); 

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmPickUp(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void testConfirmPickUp_Success() {
            // Arrange
        String bookingNumber = "B123";

        Account account = new Account();
        account.setId("123");
        Car car = new Car();
        car.setId("123");
        car.setAccount(account);

        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(account);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusMinutes(30));  
        booking.setDropOffTime(LocalDateTime.now().plusDays(1));
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

            // Mock repository
        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setDriverDrivingLicenseUrl("mock-url");
        mockResponse.setBookingNumber(bookingNumber);
        mockResponse.setCarId(car.getId());

        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);

        
        BookingResponse response = bookingService.confirmPickUp(bookingNumber);

            // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.IN_PROGRESS, booking.getStatus());

    }

    @Test
    void cancelBooking_ShouldThrowException() {
        // Arrange
        String bookingNumber = "B123";

        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        Account account = new Account();
        account.setId("123");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);


            
            AppException exception = assertThrows(AppException.class, () -> {
                bookingService.cancelBooking(bookingNumber);
            });

            assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());

    }


    @Test
    void createBooking_MissingFields_ThrowsException3() {
        // Given
        String accountId = "user123";

        CreateBookingRequest request = new CreateBookingRequest();
        request.setDriver(true);
        request.setDriverFullName("abc");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("");
        request.setDriverPhoneNumber("");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        request.setDriverDrivingLicense(mockFile);

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
        request.setCarId("car123");

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
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void createBooking_MissingFields_ThrowsException2() {
        // Given
        String accountId = "user123";

        CreateBookingRequest request = new CreateBookingRequest();
        request.setDriver(true);
        request.setDriverFullName("abc");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        request.setDriverDrivingLicense(mockFile);

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
        request.setCarId("car123");

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
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void createBooking_MissingFields_ThrowsException() {
        // Given
        String accountId = "user123";

        CreateBookingRequest request = new CreateBookingRequest();
        request.setDriver(true);
        request.setDriverFullName("");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        request.setDriverDrivingLicense(mockFile);

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
        request.setCarId("car123");

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
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException5() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("null");
        request.setDriverDob(LocalDate.of(1,1,1));
        request.setDriverNationalId("null");
        request.setDriverPhoneNumber(null);
        request.setDriverCityProvince("null");
        request.setDriverDistrict("null");
        request.setDriverWard("null");

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });
        
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException4() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("null");
        request.setDriverDob(LocalDate.of(1,1,1));
        request.setDriverNationalId(null);
        request.setDriverPhoneNumber("null");
        request.setDriverCityProvince("null");
        request.setDriverDistrict("null");
        request.setDriverWard("null");

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });
        
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }


    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException3() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("null");
        request.setDriverDob(null);
        request.setDriverNationalId("null");
        request.setDriverPhoneNumber("null");
        request.setDriverCityProvince("null");
        request.setDriverDistrict("null");
        request.setDriverWard("null");

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });
        
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException2() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);



        
        MultipartFile newMockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(newMockFile);

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });
        
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @ParameterizedTest
    @EnumSource(value = EBookingStatus.class, names = {"IN_PROGRESS", "PENDING_PAYMENT", "COMPLETED", "CANCELLED","WAITING_CONFIRMED_RETURN_CAR"})
    void editBooking_InvalidStatus_ThrowsException(EBookingStatus status) {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();

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

        
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        Car car = new Car();
        car.setId("car123");
        existingBooking.setCar(car);
        existingBooking.setStatus(status); 

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });

        
        assertEquals(ErrorCode.BOOKING_CANNOT_BE_EDITED, exception.getErrorCode());
    }

    @Test
    void editBooking_Success_WithNewDriverLicense_PendingDeposit() throws AppException, MessagingException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("Test User");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        request.setDriverWard("Kim Mã");
        request.setDriverEmail("test@gmail.com");
        request.setDriverHouseNumberStreet("123 Đường ABC");



        MultipartFile newMockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(newMockFile);

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



        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(mockPickUpTime);
        existingBooking.setDropOffTime(mockDropOffTime);
        existingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
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


        BookingResponse response = bookingService.editBooking(request, bookingNumber);


        assertNotNull(response, "Response should not be null");
        assertEquals(expectedUrl, response.getDriverDrivingLicenseUrl());
    }

    @Test
    void editBooking_Success_WithNewDriverLicense() throws AppException, MessagingException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("Test User");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        request.setDriverWard("Kim Mã");
        request.setDriverEmail("test@gmail.com");
        request.setDriverHouseNumberStreet("123 Đường ABC");


        
        MultipartFile newMockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(newMockFile);

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

        
        BookingResponse response = bookingService.editBooking(request, bookingNumber);

        
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedUrl, response.getDriverDrivingLicenseUrl());
    }
    

    @Test
    void editBooking_Failed_WithNewDriverLicense() throws AppException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("Test User");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        request.setDriverWard("Kim Mã");
        request.setDriverEmail("test@gmail.com");
        request.setDriverHouseNumberStreet("123 Đường ABC");

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });
        
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_WhenDriverInfoInvalid_ShouldThrowAppException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriver(true);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("Test User");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        request.setDriverWard("Kim Mã");


        
        MultipartFile newMockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(newMockFile);

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });
        
        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }

    @Test
    void editBooking_NotOwner_ThrowsException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();

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

        
        Account anotherAccount = new Account();
        anotherAccount.setId("otherUser");

        
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(anotherAccount); 
        Car car = new Car();
        car.setId("car123");
        existingBooking.setCar(car);
        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);

        // Mock repository
        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });

        
        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }

    @Test
    void editBooking_BookingNotFound_ThrowsException() {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        request.setDriverFullName("Updated Name");
        request.setDriverNationalId("0987654321");
        request.setDriverPhoneNumber("0771234567");

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

        
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.editBooking(request, bookingNumber);
        });

        
        assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());

        
        verify(bookingRepository, times(1)).findBookingByBookingNumber(bookingNumber);
    }

    @Test
    void editBooking_Success() throws AppException, MessagingException {
        String accountId = "user123";
        String bookingNumber = "BK123";

        // Mock request edit
        EditBookingRequest request = new EditBookingRequest();
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setDriverFullName("Updated Name");
        request.setDriverNationalId("0987654321");
        request.setDriverPhoneNumber("0771234567");

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


        
        Booking existingBooking = new Booking();
        existingBooking.setBookingNumber(bookingNumber);
        existingBooking.setAccount(mockAccount);
        existingBooking.setCar(mockCar);
        existingBooking.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0));
        existingBooking.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0));
        existingBooking.setStatus(EBookingStatus.CONFIRMED);
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

        
        BookingResponse response = bookingService.editBooking(request, bookingNumber);
        System.out.println("Booking response"+ response);
        
        assertNotNull(response, "Response should not be null");
        assertEquals(mockCar.getId(), response.getCarId());

    }

    
    @Test
    void testCreateBooking_WithDriver_ThrowException() throws AppException {
        // Given
        String accountId = "user123";

        CreateBookingRequest request = new CreateBookingRequest();
        request.setDriver(true);
        request.setDriverFullName("Test User");
        request.setDriverDob(LocalDate.of(2000, 1, 1));
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverCityProvince("Hà Nội");
        request.setDriverDistrict("Ba Đình");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        MultipartFile mockFile = mock(MultipartFile.class);
        request.setDriverDrivingLicense(mockFile);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
        request.setDriverDrivingLicense(mockFile);

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
        request.setCarId("car123");

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
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        assertEquals(ErrorCode.INVALID_DRIVER_INFO, exception.getErrorCode());
    }
    

    @Test
    void createBooking_WhenDriverInfoInvalid_ShouldThrowAppException() {
        // Given
        String accountId = "user123";

CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);
        request.setDriver(true);

        // Intentionally set invalid driver info
        request.setDriverFullName(null);  // Invalid
        request.setDriverNationalId(null);  // Invalid
        request.setDriverPhoneNumber("0886980035");  // Valid, but the other two are invalid

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
                () -> bookingService.createBooking(request));

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
    void createBooking_WhenPaymentByCashOrBankTransfer_ShouldSetStatusPendingDeposit() throws MessagingException {

        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("1");
        request.setPaymentType(EPaymentType.CASH);
        request.setPickUpTime(pickUpTime);
        request.setDropOffTime(dropOffTime);
        request.setDriverNationalId("1234567890");
        request.setDriverPhoneNumber("0987654321");
        request.setDriverFullName("Test User");


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
        when(carService.isCarAvailable(car.getId(), request.getPickUpTime(), request.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            CreateBookingRequest requestMap = invocation.getArgument(0);
            Booking mappedBooking = new Booking();
            mappedBooking.setPickUpTime(requestMap.getPickUpTime());
            mappedBooking.setDropOffTime(requestMap.getDropOffTime());
            mappedBooking.setPaymentType(requestMap.getPaymentType());
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
        BookingResponse response = bookingService.createBooking(request);


        // Then
        assertEquals(EBookingStatus.PENDING_DEPOSIT, response.getStatus());
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());
    }

    @Test
    void createBooking_ProfileIncomplete_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete1_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }
    

    @Test
    void createBooking_ProfileIncomplete4_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete3_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete5_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete6_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete7_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete8_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete9_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete10_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete11_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


        Account mockAccount = new Account();
        mockAccount.setId(accountId);


        UserProfile mockProfile = new UserProfile();

        mockProfile.setDob(null);


        mockAccount.setProfile(mockProfile);


        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);


        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete12_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete13_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete14_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete15_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }


    @Test
    void createBooking_ProfileIncomplete16_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
    }

    @Test
    void createBooking_ProfileIncomplete17_ShouldThrowException() {
        String accountId = "user123";


CreateBookingRequest request = new CreateBookingRequest();
        request.setCarId("car123");
        request.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        request.setPickUpTime(mockPickUpTime);
        request.setDropOffTime(mockDropOffTime);


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
            bookingService.createBooking(request);
        });

        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
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
        Account account = new Account();
        account.setId(accountId);
        account.setEmail("abc@gmail");

        UserProfile profile = new UserProfile();
        profile.setPhoneNumber("0886980035");

        account.setProfile(profile);

        Booking booking = new Booking();
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setBasePrice(1000);
        booking.setCar(new Car());
        booking.setAccount(account);

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
    void getBookingDetailsByBookingNumber_shouldThrowException_whenCarOwnerBookingNotFound() {
        // Given
        Account owner = new Account();
        owner.setId("user123");
        Role role = new Role();
        role.setName(ERole.CAR_OWNER);
        owner.setRole(role);

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(owner);
        lenient().when(bookingRepository.findBookingByBookingNumberAndOwnerId("BK001", "user123")).thenReturn(null);

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
    void getBookingsOfCustomer_WithValidStatus_ReturnsBookingListResponse() {
        // Given
        String status = "CONFIRMED";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Account account = new Account();
        account.setId(accountId);
        account.setEmail("abc@gmail");

        UserProfile profile = new UserProfile();
        profile.setPhoneNumber("0886980035");

        account.setProfile(profile);

        Booking booking = new Booking();
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1));
        booking.setDropOffTime(LocalDateTime.now().plusDays(3));
        booking.setBasePrice(1000);
        booking.setCar(new Car());
        booking.setAccount(account);

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
        String status = "INVALID_STATUS"; 
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Booking> bookingPage = new PageImpl<>(Collections.emptyList());

        when(bookingRepository.findByAccountId(eq(accountId), eq(pageable)))
                .thenReturn(bookingPage);

        // When
        BookingListResponse response = bookingService.getBookingsOfCustomer(0, 10, "updatedAt,DESC", status);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getBookings().getTotalElements());
        verify(bookingRepository).findByAccountId(eq(accountId), eq(pageable)); 
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
}