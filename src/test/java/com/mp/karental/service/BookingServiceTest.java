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
    void testConfirmBooking_StatusNotWaitingConfirm_ThrowsException() throws MessagingException {
        // Arrange
        String bookingNumber = "BK123456";

        // Mock chủ xe
        Account owner = new Account();
        owner.setId("1L"); // Sử dụng Long thay vì String

        // Mock xe thuộc chủ xe
        Car car = new Car();
        car.setId("101L");
        car.setAccount(owner);
        car.setBrand("Toyota");
        car.setModel("Camry");

        // Mock khách hàng đặt xe
        Account customer = new Account();
        customer.setId("2L");
        customer.setEmail("customer@example.com");

        // Mock booking
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(customer);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.COMPLETED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1)); // Chưa quá hạn
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        System.out.println("pick up time: " + booking.getPickUpTime());
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        // Mock hành vi của repository
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().doAnswer(invocation -> invocation.getArgument(0)).when(bookingRepository).saveAndFlush(any(Booking.class));

        // Mock SecurityUtil (nếu cần)

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(owner);

        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmBooking(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testConfirmBooking_StatusNull_ThrowsException() throws MessagingException {
        // Arrange
        String bookingNumber = "BK123456";

        // Mock chủ xe
        Account owner = new Account();
        owner.setId("1L"); // Sử dụng Long thay vì String

        // Mock xe thuộc chủ xe
        Car car = new Car();
        car.setId("101L");
        car.setAccount(owner);
        car.setBrand("Toyota");
        car.setModel("Camry");

        // Mock khách hàng đặt xe
        Account customer = new Account();
        customer.setId("2L");
        customer.setEmail("customer@example.com");

        // Mock booking
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(customer);
        booking.setCar(car);
        booking.setStatus(null);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1)); // Chưa quá hạn
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        System.out.println("pick up time: " + booking.getPickUpTime());
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        // Mock hành vi của repository
        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        lenient().doAnswer(invocation -> invocation.getArgument(0)).when(bookingRepository).saveAndFlush(any(Booking.class));

        // Mock SecurityUtil (nếu cần)

        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(owner);

        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.confirmBooking(bookingNumber);
        });

        assertEquals(ErrorCode.INVALID_BOOKING_STATUS, exception.getErrorCode());
    }

    @Test
    void testConfirmBooking_Success() throws MessagingException {
        // Arrange
        String bookingNumber = "BK123456";

        // Mock chủ xe
        Account owner = new Account();
        owner.setId("1L"); // Sử dụng Long thay vì String

        // Mock xe thuộc chủ xe
        Car car = new Car();
        car.setId("101L");
        car.setAccount(owner);
        car.setBrand("Toyota");
        car.setModel("Camry");

        // Mock khách hàng đặt xe
        Account customer = new Account();
        customer.setId("2L");
        customer.setEmail("customer@example.com");

        // Mock booking
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(customer);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusDays(1)); // Chưa quá hạn
        booking.setDropOffTime(LocalDateTime.now().plusDays(2));
        System.out.println("pick up time: " + booking.getPickUpTime());
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        // Mock hành vi của repository
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        doAnswer(invocation -> invocation.getArgument(0)).when(bookingRepository).saveAndFlush(any(Booking.class));

        // Mock SecurityUtil (nếu cần)

        when(SecurityUtil.getCurrentAccount()).thenReturn(owner);
        // Act
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setDriverDrivingLicenseUrl("mock-url");
        mockResponse.setBookingNumber(bookingNumber);
        mockResponse.setStatus(EBookingStatus.CONFIRMED);

        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);
            // Act
            BookingResponse response = bookingService.confirmBooking(bookingNumber);

            // Assert
            assertNotNull(response);
            assertEquals(EBookingStatus.CONFIRMED, booking.getStatus());

            // Kiểm tra repository được gọi chính xác
            verify(bookingRepository, times(1)).saveAndFlush(booking);

            // Kiểm tra email được gửi đúng cách
            verify(emailService, times(1)).sendConfirmBookingEmail(
                    eq(customer.getEmail()),
                    eq("Toyota Camry"),
                    eq(bookingNumber)
            );

    }

    @Test
    void testConfirmBooking_WhenBookingNotFound() {
        // Arrange
        String bookingNumber = "B123";
        Account account = new Account();
        account.setId("123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null); // ✅ Giả lập không tìm thấy booking

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

        // Tạo tài khoản khách hàng
        Account customerAccount = new Account();
        customerAccount.setId(userId);
        Role customerRole = new Role();
        customerRole.setName(ERole.CUSTOMER);
        customerAccount.setRole(customerRole);
        Car car = new Car();
        car.setId("123");
        // Tạo booking gán với tài khoản khách hàng
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

        // Act: Gọi cancelBooking()
        BookingResponse response = bookingService.getBookingDetailsByBookingNumber(bookingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(bookingNumber, response.getBookingNumber());
    }

    @Test
    void testGetBookingDetailsByBookingNumber_UnauthorizedAccess() {
        // Arrange
        String bookingNumber = "B123";
        String userId = "999"; // Người dùng không phải CAR_OWNER hay CUSTOMER

        // Tạo tài khoản với vai trò không hợp lệ
        Account unauthorizedAccount = new Account();
        unauthorizedAccount.setId(userId);
        Role unauthorizedRole = new Role();
        unauthorizedRole.setName(ERole.ADMIN); // Giả sử ADMIN không được phép truy cập
        unauthorizedAccount.setRole(unauthorizedRole);

        // Mock SecurityUtil để giả lập người dùng đăng nhập với vai trò không hợp lệ
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
        String ownerId = "123"; // Chủ xe hợp lệ
        String wrongOwnerId = "999"; // Người dùng không sở hữu xe

        // Tạo tài khoản của chủ xe
        Account carOwnerAccount = new Account();
        carOwnerAccount.setId(wrongOwnerId); // Không phải chủ xe hợp lệ
        Role carOwnerRole = new Role();
        carOwnerRole.setName(ERole.CAR_OWNER);
        carOwnerAccount.setRole(carOwnerRole);

        // Tạo xe thuộc về chủ xe thực sự (ID "123")
        Account actualCarOwner = new Account();
        actualCarOwner.setId(ownerId);
        Car car = new Car();
        car.setId("CAR123");
        car.setAccount(actualCarOwner);

        // Tạo booking gắn với xe
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setCar(car);

        // Mock SecurityUtil để giả lập người dùng đăng nhập có ID "999"
        when(SecurityUtil.getCurrentAccountId()).thenReturn(wrongOwnerId);
        when(SecurityUtil.getCurrentAccount()).thenReturn(carOwnerAccount);

        // Mock repository để trả về booking
        when(bookingRepository.findBookingByBookingNumberAndOwnerId(bookingNumber, wrongOwnerId)).thenReturn(booking);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.getBookingDetailsByBookingNumber(bookingNumber);
        });

        assertEquals(ErrorCode.FORBIDDEN_CAR_ACCESS, exception.getErrorCode());
    }

    @Test
    void testCancelBooking_BookingCannotBeCancelled_CANCELLED() throws MessagingException {
        // Arrange
        String bookingNumber = "B123";
        Account mockAccount = new Account();
        mockAccount.setId("user123");
        mockAccount.setEmail("test@example.com");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        Account account = new Account();
        account.setId("123");
        account.setEmail("test@example.com");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.CANCELLED);
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        Wallet walletCustomer = new Wallet();
        walletCustomer.setId(mockAccount.getId());

        Account adminAccount = new Account();
        adminAccount.setId("admin123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.cancelBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_CANNOT_CANCEL, exception.getErrorCode());
    }

    @Test
    void testCancelBooking_BookingCannotBeCancelled_COMPLETED() throws MessagingException {
        // Arrange
        String bookingNumber = "B123";
        Account mockAccount = new Account();
        mockAccount.setId("user123");
        mockAccount.setEmail("test@example.com");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        Account account = new Account();
        account.setId("123");
        account.setEmail("test@example.com");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.COMPLETED);
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        Wallet walletCustomer = new Wallet();
        walletCustomer.setId(mockAccount.getId());

        Account adminAccount = new Account();
        adminAccount.setId("admin123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.cancelBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_CANNOT_CANCEL, exception.getErrorCode());
    }

    @Test
    void testCancelBooking_BookingCannotBeCancelled_PENDING_PAYMENT() throws MessagingException {
        // Arrange
        String bookingNumber = "B123";
        Account mockAccount = new Account();
        mockAccount.setId("user123");
        mockAccount.setEmail("test@example.com");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        Account account = new Account();
        account.setId("123");
        account.setEmail("test@example.com");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.PENDING_PAYMENT);
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        Wallet walletCustomer = new Wallet();
        walletCustomer.setId(mockAccount.getId());

        Account adminAccount = new Account();
        adminAccount.setId("admin123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.cancelBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_CANNOT_CANCEL, exception.getErrorCode());
    }

    @Test
    void testCancelBooking_BookingCannotBeCancelled_InProgress() throws MessagingException {
        // Arrange
        String bookingNumber = "B123";
        Account mockAccount = new Account();
        mockAccount.setId("user123");
        mockAccount.setEmail("test@example.com");
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        Account account = new Account();
        account.setId("123");
        account.setEmail("test@example.com");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.IN_PROGRESS);
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        Wallet walletCustomer = new Wallet();
        walletCustomer.setId(mockAccount.getId());

        Account adminAccount = new Account();
        adminAccount.setId("admin123");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.cancelBooking(bookingNumber);
        });

        assertEquals(ErrorCode.BOOKING_CANNOT_CANCEL, exception.getErrorCode());
    }

    @Test
    void testConfirmPickUp_BookingCannotBePickedUp_TooLate() {
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
        booking.setPickUpTime(LocalDateTime.now().minusDays(2)); // Quá hạn pick up
        booking.setDropOffTime(LocalDateTime.now().minusDays(1)); // ❌ Drop-off đã qua

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
    void testConfirmPickUp_BookingCannotBePickedUp_TooEarly() {
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
        booking.setPickUpTime(LocalDateTime.now().plusHours(2)); // ❌ Còn hơn 30 phút nữa mới đến pickUpTime
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
        booking.setStatus(EBookingStatus.CANCELLED); // ❌ Không phải CONFIRMED
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
        account1.setId("123"); // Người đặt booking

        Account account2 = new Account();
        account2.setId("999"); // Người đang đăng nhập (không phải chủ booking)

        Car car = new Car();
        car.setId("123");
        car.setAccount(account1); // Booking thuộc về account1

        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(account1); // Booking thuộc về account1
        booking.setCar(car);
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setPickUpTime(LocalDateTime.now().plusMinutes(30));
        booking.setDropOffTime(LocalDateTime.now().plusDays(1));
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("999"); // ✅ Người dùng đăng nhập là account2
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

        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(null); // ✅ Giả lập không tìm thấy booking

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
        booking.setPickUpTime(LocalDateTime.now().plusMinutes(30));  // ✅ Đặt pickUpTime tại đây
        booking.setDropOffTime(LocalDateTime.now().plusDays(1));
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

            // Mock repository
        when(SecurityUtil.getCurrentAccountId()).thenReturn("123");
        when(SecurityUtil.getCurrentAccount()).thenReturn(account);
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);

        // Mock BookingResponse với giá trị hợp lệ
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setDriverDrivingLicenseUrl("mock-url");
        mockResponse.setBookingNumber(bookingNumber);
        mockResponse.setCarId(car.getId());

        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);

        // Act: Gọi cancelBooking()
        BookingResponse response = bookingService.confirmPickUp(bookingNumber);

            // Assert
        assertNotNull(response);
        assertEquals(EBookingStatus.IN_PROGRESS, booking.getStatus());

    }

//    @Test
//    void cancelBooking_ShouldCancelCofirmedBooking() throws MessagingException {
//        // Arrange
//        String bookingNumber = "B123";
//        Account mockAccount = new Account();
//        mockAccount.setId("user123");
//        mockAccount.setEmail("test@example.com");
//
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//
//        Account account = new Account();
//        account.setId("123");
//        account.setEmail("test@example.com");
//        Car car = new Car();
//        car.setBrand("Toyota");
//        car.setModel("Camry");
//        car.setAccount(account);
//
//
//
//        Booking booking = new Booking();
//        booking.setBookingNumber(bookingNumber);
//        booking.setAccount(mockAccount);
//        booking.setCar(car);
//        booking.setStatus(EBookingStatus.CONFIRMED);
//        booking.setPickUpTime(mockPickUpTime);
//        booking.setDropOffTime(mockDropOffTime);
//        booking.setDriverDrivingLicenseUri("user/abc.jpg");
//
//        Wallet walletCustomer = new Wallet();
//        walletCustomer.setId(mockAccount.getId());
//
//        Account adminAccount = new Account();
//        adminAccount.setId("admin123");
//
//        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
//        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
//        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
//        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
//        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));
//
//        // Mock BookingResponse với giá trị hợp lệ
//        BookingResponse mockResponse = new BookingResponse();
//        mockResponse.setDriverDrivingLicenseUrl("mock-url");
//        mockResponse.setBookingNumber(bookingNumber);
//        mockResponse.setCarId(car.getId());
//
//        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);
//
//        // Act: Gọi cancelBooking()
//        BookingResponse response = bookingService.cancelBooking(bookingNumber);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
//        verify(emailService).sendCustomerBookingCanceledWithPartialRefundEmail(mockAccount.getEmail(), "Toyota Camry");
//
//        verify(emailService).sendCarOwnerBookingCanceledEmail(car.getAccount().getEmail(), "Toyota Camry");
//    }

    @Test
    void cancelBooking_ShouldThrowException2() {
        // Arrange
        String bookingNumber = "B123";
        Account mockAccount = new Account();
        mockAccount.setId("1"); // Đảm bảo trùng với SecurityUtil.getCurrentAccountId()
        mockAccount.setEmail("test@example.com");

        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        Account account = new Account();
        account.setId("123");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);

        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setAccount(mockAccount);
        booking.setCar(car);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
        booking.setPickUpTime(mockPickUpTime);
        booking.setDropOffTime(mockDropOffTime);
        booking.setDriverDrivingLicenseUri("user/abc.jpg");

        when(SecurityUtil.getCurrentAccountId()).thenReturn("2"); // Không trùng với booking owner
        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking); // Đảm bảo booking được tìm thấy

        // Act & Assert: Kiểm tra xem exception có bị ném ra không
        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.cancelBooking(booking.getBookingNumber());
        });

        assertEquals(ErrorCode.FORBIDDEN_BOOKING_ACCESS, exception.getErrorCode());
    }


    @Test
    void cancelBooking_ShouldThrowException() {
        // Arrange
        String bookingNumber = "B123";
        Account mockAccount = new Account();
        mockAccount.setId("user123");
        mockAccount.setEmail("test@example.com");

        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);

        Account account = new Account();
        account.setId("123");
        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setAccount(account);


            // Act & Assert: Kiểm tra xem exception có bị ném ra không
            AppException exception = assertThrows(AppException.class, () -> {
                bookingService.cancelBooking(bookingNumber);
            });

            assertEquals(ErrorCode.BOOKING_NOT_FOUND_IN_DB, exception.getErrorCode());

    }

//    @Test
//    void cancelBooking_ShouldCancelWaitingCofirmBooking() throws MessagingException {
//        // Arrange
//        String bookingNumber = "B123";
//        Account mockAccount = new Account();
//        mockAccount.setId("user123");
//        mockAccount.setEmail("test@example.com");
//
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//
//        Account account = new Account();
//        account.setId("123");
//        Car car = new Car();
//        car.setBrand("Toyota");
//        car.setModel("Camry");
//        car.setAccount(account);
//
//
//
//        Booking booking = new Booking();
//        booking.setBookingNumber(bookingNumber);
//        booking.setAccount(mockAccount);
//        booking.setCar(car);
//        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
//        booking.setPickUpTime(mockPickUpTime);
//        booking.setDropOffTime(mockDropOffTime);
//        booking.setDriverDrivingLicenseUri("user/abc.jpg");
//
//        Wallet walletCustomer = new Wallet();
//        walletCustomer.setId(mockAccount.getId());
//
//        Account adminAccount = new Account();
//        adminAccount.setId("admin123");
//
//        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
//        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
//        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
//        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
//        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));
//
//        // Mock BookingResponse với giá trị hợp lệ
//        BookingResponse mockResponse = new BookingResponse();
//        mockResponse.setDriverDrivingLicenseUrl("mock-url");
//        mockResponse.setBookingNumber(bookingNumber);
//        mockResponse.setCarId(car.getId());
//
//        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);
//
//        // Act: Gọi cancelBooking()
//        BookingResponse response = bookingService.cancelBooking(bookingNumber);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
//        verify(emailService).sendCustomerBookingCanceledWithFullRefundEmail(mockAccount.getEmail(), "Toyota Camry");
//    }


//    @Test
//    void cancelBooking_ShouldCancelPendingDepositBooking() throws MessagingException {
//        // Arrange
//        String bookingNumber = "B123";
//        Account mockAccount = new Account();
//        mockAccount.setId("user123");
//        mockAccount.setEmail("test@example.com");
//
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//
//        Account account = new Account();
//        account.setId("123");
//        Car car = new Car();
//        car.setBrand("Toyota");
//        car.setModel("Camry");
//        car.setAccount(account);
//
//        Booking booking = new Booking();
//        booking.setBookingNumber(bookingNumber);
//        booking.setAccount(mockAccount);
//        booking.setCar(car);
//        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
//        booking.setPickUpTime(mockPickUpTime);
//        booking.setDropOffTime(mockDropOffTime);
//        booking.setDriverDrivingLicenseUri("user/abc.jpg");
//
//        Wallet walletCustomer = new Wallet();
//        walletCustomer.setId(mockAccount.getId());
//
//        Account adminAccount = new Account();
//        adminAccount.setId("admin123");
//
//        when(SecurityUtil.getCurrentAccountId()).thenReturn("user123");
//        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//        when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(booking);
//        when(walletRepository.findById("user123")).thenReturn(Optional.of(walletCustomer));
//        when(accountRepository.findByRoleId(3)).thenReturn(adminAccount);
//        when(walletRepository.findById("admin123")).thenReturn(Optional.of(new Wallet()));
//
//        // Mock BookingResponse với giá trị hợp lệ
//        BookingResponse mockResponse = new BookingResponse();
//        mockResponse.setDriverDrivingLicenseUrl("mock-url");
//        mockResponse.setBookingNumber(bookingNumber);
//        mockResponse.setCarId(car.getId());
//
//        when(bookingMapper.toBookingResponse(any())).thenReturn(mockResponse);
//
//        // Act: Gọi cancelBooking()
//        BookingResponse response = bookingService.cancelBooking(bookingNumber);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(EBookingStatus.CANCELLED, booking.getStatus());
//        verify(emailService).sendCustomerBookingCanceledEmail(mockAccount.getEmail(), "Toyota Camry");
//    }

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
    void editBooking_Success_WithNewDriverLicense() throws AppException, MessagingException {
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


//    @Test
//    void editBooking_Success_WithOldDriverLicense() throws AppException, MessagingException {
//        String accountId = "user123";
//        String bookingNumber = "BK123";
//
//        // Mock request edit
//        EditBookingRequest CreateBookingRequest = new EditBookingRequest();
//        CreateBookingRequest.setDriver(true);
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//        CreateBookingRequest.setDriverFullName("Test User");
//        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
//        CreateBookingRequest.setDriverNationalId("1234567890");
//        CreateBookingRequest.setDriverPhoneNumber("0987654321");
//        CreateBookingRequest.setDriverCityProvince("Hà Nội");
//        CreateBookingRequest.setDriverDistrict("Ba Đình");
//        CreateBookingRequest.setDriverWard("Kim Mã");
//        CreateBookingRequest.setDriverEmail("test@gmail.com");
//        CreateBookingRequest.setDriverHouseNumberStreet("123 Đường ABC");
//
//
//        // Mock account
//        Account mockAccount = new Account();
//        mockAccount.setId(accountId);
//
//        // Mock user profile
//        UserProfile mockProfile = new UserProfile();
//        mockProfile.setFullName("Test User");
//        mockProfile.setDob(LocalDate.of(2000, 1, 1));
//        mockProfile.setNationalId("1234567890");
//        mockProfile.setPhoneNumber("0987654321");
//        mockProfile.setCityProvince("Hà Nội");
//        mockProfile.setDistrict("Ba Đình");
//        mockProfile.setWard("Kim Mã");
//        mockProfile.setHouseNumberStreet("123 Đường ABC");
//        mockProfile.setDrivingLicenseUri("license.jpg");
//
//        mockAccount.setProfile(mockProfile);
//
//        // Mock car
//        Car mockCar = new Car();
//        mockCar.setId("car123");
//        mockCar.setDeposit(5000);
//        mockCar.setBasePrice(2000);
//
//        CreateBookingRequest.setCarId(mockCar.getId());
//
//        // Mock booking hiện tại trong DB
//        Booking existingBooking = new Booking();
//        existingBooking.setBookingNumber(bookingNumber);
//        existingBooking.setAccount(mockAccount);
//        existingBooking.setCar(mockCar);
//        existingBooking.setPickUpTime(mockPickUpTime);
//        existingBooking.setDropOffTime(mockDropOffTime);
//        existingBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
//        existingBooking.setDriverDrivingLicenseUri("old-license.jpg");
//
//        // Mock repository
//        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//        lenient().when(bookingRepository.findBookingByBookingNumber(bookingNumber)).thenReturn(existingBooking);
//        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Mock file upload
//        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
//        String expectedUrl = "https://s3-bucket.com/dummy-url.jpg";
//
//        lenient().when(fileService.getFileUrl("old-license.jpg")).thenReturn("https://s3-bucket.com/old-license.jpg");
//        lenient().when(fileService.getFileUrl("booking/BK123/driver-driving-license.jpg")).thenReturn("https://s3-bucket.com/booking/BK123/driver-driving-license.jpg");
//
//
//        // Mock mapper
//        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
//            Booking updatedBooking = invocation.getArgument(0);
//            BookingResponse response = new BookingResponse();
//            response.setPickUpTime(updatedBooking.getPickUpTime());
//            response.setDropOffTime(updatedBooking.getDropOffTime());
//            response.setCarId(updatedBooking.getCar().getId());
//            response.setDriverDrivingLicenseUrl(existingBooking.getDriverDrivingLicenseUri());
//            return response;
//        });
//
//        // Thực thi service
//        BookingResponse response = bookingService.editBooking(CreateBookingRequest, bookingNumber);
//
//        // Kiểm tra kết quả
//        assertNotNull(response, "Response should not be null");
//        assertEquals("https://s3-bucket.com/old-license.jpg", response.getDriverDrivingLicenseUrl());
//    }

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
    void editBooking_Success() throws AppException, MessagingException {
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

//    @Test
//    void testCreateBooking_WithDriver() throws AppException, MessagingException {
//        // Given
//        String accountId = "user123";
//
//        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
//        CreateBookingRequest.setDriver(true);
//        CreateBookingRequest.setDriverFullName("Test User");
//        CreateBookingRequest.setDriverDob(LocalDate.of(2000, 1, 1));
//        CreateBookingRequest.setDriverNationalId("1234567890");
//        CreateBookingRequest.setDriverPhoneNumber("0987654321");
//        CreateBookingRequest.setDriverCityProvince("Hà Nội");
//        CreateBookingRequest.setDriverDistrict("Ba Đình");
//        CreateBookingRequest.setDriverWard("Kim Mã");
//        CreateBookingRequest.setDriverEmail("test@gmail.com");
//        CreateBookingRequest.setDriverHouseNumberStreet("123 Đường ABC");
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//
//        MultipartFile mockFile = mock(MultipartFile.class);
//        CreateBookingRequest.setDriverDrivingLicense(mockFile);
//        when(mockFile.isEmpty()).thenReturn(false);
//        when(fileService.getFileExtension(mockFile)).thenReturn(".jpg");
//        CreateBookingRequest.setDriverDrivingLicense(mockFile);
//
//        Account mockAccount = new Account();
//        mockAccount.setEmail("test@gmail.com");
//        mockAccount.setId(accountId);
//
//        UserProfile mockProfile = new UserProfile();
//        mockProfile.setFullName("Test User");
//        mockProfile.setDob(LocalDate.of(2000, 1, 1));
//        mockProfile.setNationalId("1234567890");
//        mockProfile.setPhoneNumber("0987654321");
//        mockProfile.setCityProvince("Hà Nội");
//        mockProfile.setDistrict("Ba Đình");
//        mockProfile.setWard("Kim Mã");
//        mockProfile.setHouseNumberStreet("123 Đường ABC");
//        mockProfile.setDrivingLicenseUri("license.jpg");
//
//        mockAccount.setProfile(mockProfile);
//
//        Wallet wallet = new Wallet();
//        wallet.setId(accountId);
//        wallet.setBalance(10000);
//
//        Account accountOwner = new Account();
//        accountOwner.setId("123");
//        accountOwner.setEmail("test@gmail.com");
//        Car mockCar = new Car();
//        mockCar.setId("car123");
//        mockCar.setBrand("Toyota");
//        mockCar.setModel("Camry");
//        mockCar.setLicensePlate("ABC-1234");
//        mockCar.setDeposit(5000);
//        mockCar.setBasePrice(2000);
//        mockCar.setAccount(accountOwner);
//        CreateBookingRequest.setCarId("car123");
//
//        Booking booking = new Booking();
//        booking.setAccount(mockAccount);
//        booking.setCar(mockCar);
//        booking.setPaymentType(EPaymentType.WALLET);
//        booking.setDeposit(5000);
//        booking.setBasePrice(2000);
//        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
//        booking.setBookingNumber("BK123");
//        booking.setPickUpTime(mockPickUpTime);
//        booking.setDropOffTime(mockDropOffTime);
//
//        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
//        when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
//        when(bookingMapper.toBooking(any())).thenReturn(booking);
//        when(redisUtil.generateBookingNumber()).thenReturn("BK123");
//        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);
//
//        String expectedS3Key = "booking/BK123/driver-driving-license.jpg";
//        String expectedUrl = "https://s3-bucket.com/" + expectedS3Key;
//
//        when(fileService.getFileUrl(expectedS3Key)).thenReturn(expectedUrl);
//        when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());
//
//        // When
//        BookingResponse response = bookingService.createBooking(CreateBookingRequest);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(expectedUrl, response.getDriverDrivingLicenseUrl());
//        verify(fileService).uploadFile(mockFile, expectedS3Key);
//        verify(bookingRepository, times(1)).save(any());
//        // ✅ Kiểm tra email gửi đến khách hàng
//        verify(emailService).sendBookingWaitingForConfirmationEmail(
//                eq(mockAccount.getEmail()),
//                eq("Toyota Camry"),
//                eq("BK123")
//        );
//
//        // ✅ Kiểm tra email gửi đến chủ xe
//        verify(emailService).sendCarOwnerConfirmationRequestEmail(
//                eq(accountOwner.getEmail()),
//                eq("Toyota Camry"),
//                eq("ABC-1234")
//        );
//    }

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

//    @Test
//    void createBooking_Success() throws AppException, MessagingException {
//        String accountId = "user123";
//
//        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
//        CreateBookingRequest.setCarId("car123");
//        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//        CreateBookingRequest.setPickUpTime(mockPickUpTime);
//        CreateBookingRequest.setDropOffTime(mockDropOffTime);
//        CreateBookingRequest.setDriverFullName("test");
//        CreateBookingRequest.setDriverNationalId("1234567890");
//        CreateBookingRequest.setDriverPhoneNumber("0886980035");
//
//        Account mockAccount = new Account();
//        mockAccount.setId(accountId);
//
//        UserProfile mockProfile = new UserProfile();
//        mockProfile.setFullName("Test User");
//        mockProfile.setDob(LocalDate.of(2000, 1, 1));
//        mockProfile.setNationalId("1234567890");
//        mockProfile.setPhoneNumber("0987654321");
//        mockProfile.setCityProvince("Hà Nội");
//        mockProfile.setDistrict("Ba Đình");
//        mockProfile.setWard("Kim Mã");
//        mockProfile.setHouseNumberStreet("123 Đường ABC");
//        mockProfile.setDrivingLicenseUri("license.jpg");
//
//        mockAccount.setProfile(mockProfile);
//
//        Wallet wallet = new Wallet();
//        wallet.setId(accountId);
//        wallet.setBalance(10000);
//
//        Car car = new Car();
//        car.setLicensePlate("ABC-1234");
//        car.setId("car123");
//        car.setBrand("Toyota");
//        car.setModel("Camry");
//        car.setDeposit(5000);
//        car.setBasePrice(2000);
//        Account accountCarOwner = new Account();
//        accountCarOwner.setId("123");
//        car.setAccount(accountCarOwner);
//
//        MultipartFile mockDrivingLicense = mock(MultipartFile.class);
//        lenient().when(mockDrivingLicense.isEmpty()).thenReturn(false);
//        lenient().when(fileService.getFileExtension(any(MultipartFile.class))).thenReturn(".jpg");
//        CreateBookingRequest.setDriverDrivingLicense(mockDrivingLicense);
//
//        Booking booking = new Booking();
//        booking.setAccount(mockAccount);
//        booking.setCar(car);
//        booking.setPaymentType(EPaymentType.WALLET);
//        booking.setDeposit(5000);
//        booking.setBasePrice(2000);
//        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
//        booking.setBookingNumber("BK123");
//        booking.setPickUpTime(mockPickUpTime);
//        booking.setDropOffTime(mockDropOffTime);
//
//        lenient().when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//        lenient().when(carRepository.findById("car123")).thenReturn(Optional.of(car));
//        lenient().when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
//        lenient().when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);
//        lenient().when(bookingMapper.toBooking(any())).thenReturn(booking);
//        lenient().when(redisUtil.generateBookingNumber()).thenReturn("B123");
//        lenient().when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());
//
//        lenient().doAnswer(invocation -> {
//            wallet.setBalance(wallet.getBalance() - car.getDeposit());
//            return null;
//        }).when(transactionService).payDeposit(accountId, car.getDeposit(), booking);
//
//
//        BookingResponse response = bookingService.createBooking(CreateBookingRequest);
//
//        assertNotNull(response);
//        assertEquals(5000, wallet.getBalance());
//        verify(transactionService, times(1)).payDeposit(accountId, car.getDeposit(), booking);
//        verify(walletRepository, times(1)).save(wallet);
//        verify(bookingRepository, times(1)).save(any());
//        // ✅ Kiểm tra email gửi đến khách hàng
//        verify(emailService).sendBookingWaitingForConfirmationEmail(
//                eq(mockAccount.getEmail()),
//                eq("Toyota Camry"),
//                eq("B123")
//        );
//
//        // ✅ Kiểm tra email gửi đến chủ xe
//        verify(emailService).sendCarOwnerConfirmationRequestEmail(
//                eq(car.getAccount().getEmail()),
//                eq("Toyota Camry"),
//                eq("ABC-1234")
//        );
//
//    }

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

//    @Test
//    void createBooking_WhenWalletHasEnoughBalance_ShouldSetStatusWaitingConfirm() throws MessagingException {
//        String accountId = "testAccountId";
//        LocalDateTime pickUpTime = LocalDateTime.now().plusDays(1);
//        LocalDateTime dropOffTime = pickUpTime.plusDays(1);
//
//        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
//        CreateBookingRequest.setCarId("1");
//        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
//        CreateBookingRequest.setPickUpTime(pickUpTime);
//        CreateBookingRequest.setDropOffTime(dropOffTime);
//        CreateBookingRequest.setDriverFullName("Test User");
//        CreateBookingRequest.setDriverNationalId("1234567890");
//        CreateBookingRequest.setDriverPhoneNumber("0987654321");
//
//        Account mockCustomerAccount = new Account();
//        mockCustomerAccount.setId(accountId);
//        mockCustomerAccount.setEmail("customer@example.com");
//
//        UserProfile mockProfile = new UserProfile();
//        mockProfile.setFullName("Test User");
//        mockProfile.setDob(LocalDate.of(2000, 1, 1));
//        mockProfile.setNationalId("1234567890");
//        mockProfile.setPhoneNumber("0987654321");
//        mockProfile.setCityProvince("Hà Nội");
//        mockProfile.setDistrict("Ba Đình");
//        mockProfile.setWard("Kim Mã");
//        mockProfile.setHouseNumberStreet("123 Đường ABC");
//        mockProfile.setDrivingLicenseUri("license.jpg");
//
//        mockCustomerAccount.setProfile(mockProfile);
//
//        Account mockCarOwnerAccount = new Account();
//        mockCarOwnerAccount.setId("ownerId");
//        mockCarOwnerAccount.setEmail("owner@example.com");
//
//        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockCustomerAccount);
//
//        Car car = new Car();
//        car.setId("1");
//        car.setBrand("Toyota");
//        car.setModel("Camry");
//        car.setLicensePlate("ABC-1234");
//        car.setDeposit(1000L);
//        car.setAccount(mockCarOwnerAccount);
//
//        Wallet wallet = new Wallet();
//        wallet.setId(accountId);
//        wallet.setBalance(5000L);
//
//        when(carRepository.findById("1")).thenReturn(Optional.of(car));
//        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
//        when(carService.isCarAvailable(car.getId(), CreateBookingRequest.getPickUpTime(), CreateBookingRequest.getDropOffTime()))
//                .thenReturn(true);
//        when(redisUtil.generateBookingNumber()).thenReturn("B123");
//
//        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
//            CreateBookingRequest request = invocation.getArgument(0);
//            Booking mappedBooking = new Booking();
//            mappedBooking.setPickUpTime(request.getPickUpTime());
//            mappedBooking.setDropOffTime(request.getDropOffTime());
//            mappedBooking.setPaymentType(request.getPaymentType());
//            mappedBooking.setStatus(EBookingStatus.WAITING_CONFIRMED);
//            mappedBooking.setCar(car);
//            return mappedBooking;
//        });
//
//        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
//            Booking booking = invocation.getArgument(0);
//            BookingResponse response = new BookingResponse();
//            response.setBookingNumber("B123");
//            response.setCarId(booking.getCar().getId());
//            response.setStatus(booking.getStatus());
//            response.setPickUpTime(booking.getPickUpTime());
//            response.setDropOffTime(booking.getDropOffTime());
//            response.setTotalPrice(2000L);
//            response.setDeposit(booking.getCar().getDeposit());
//            response.setPaymentType(EPaymentType.WALLET);
//            response.setDriverDrivingLicenseUrl("dummyUrl");
//            return response;
//        });
//
//        doAnswer(invocation -> {
//            wallet.setBalance(wallet.getBalance() - car.getDeposit());
//            return null;
//        }).when(transactionService).payDeposit(eq(accountId), eq(car.getDeposit()), any());
//
//        // When
//        BookingResponse response = bookingService.createBooking(CreateBookingRequest);
//
//        // Then
//        assertEquals(EBookingStatus.WAITING_CONFIRMED, response.getStatus());
//        assertEquals(4000L, wallet.getBalance());
//        assertEquals(pickUpTime, response.getPickUpTime());
//        assertEquals(dropOffTime, response.getDropOffTime());
//
//        verify(walletRepository, atMostOnce()).save(any());
//        verify(transactionService).payDeposit(eq(accountId), eq(car.getDeposit()), any());
//
//        // ✅ Kiểm tra email gửi đến khách hàng
//        verify(emailService).sendBookingWaitingForConfirmationEmail(
//                eq("customer@example.com"),
//                eq("Toyota Camry"),
//                eq("B123")
//        );
//
//        // ✅ Kiểm tra email gửi đến chủ xe
//        verify(emailService).sendCarOwnerConfirmationRequestEmail(
//                eq("owner@example.com"),
//                eq("Toyota Camry"),
//                eq("ABC-1234")
//        );
//    }

//    @Test
//    void createBooking_WhenWalletHasNotEnoughBalance_ShouldSetStatusPendingDeposit() throws MessagingException {
//        // Given
//        String accountId = "testAccountId";
//        LocalDateTime pickUpTime = LocalDateTime.now().plusDays(1);
//        LocalDateTime dropOffTime = pickUpTime.plusDays(1);
//
//        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
//        CreateBookingRequest.setCarId("1");
//        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
//        CreateBookingRequest.setPickUpTime(pickUpTime);
//        CreateBookingRequest.setDropOffTime(dropOffTime);
//        CreateBookingRequest.setDriverNationalId("1234567890");
//        CreateBookingRequest.setDriverPhoneNumber("0987654321");
//        CreateBookingRequest.setDriverFullName("Test User");
//
//        Account mockAccount = new Account();
//        mockAccount.setId(accountId);
//
//        UserProfile mockProfile = new UserProfile();
//        mockProfile.setFullName("Test User");
//        mockProfile.setDob(LocalDate.of(2000, 1, 1));
//        mockProfile.setNationalId("1234567890");
//        mockProfile.setPhoneNumber("0987654321");
//        mockProfile.setCityProvince("Hà Nội");
//        mockProfile.setDistrict("Ba Đình");
//        mockProfile.setWard("Kim Mã");
//        mockProfile.setHouseNumberStreet("123 Đường ABC");
//        mockProfile.setDrivingLicenseUri("license.jpg");
//
//        mockAccount.setProfile(mockProfile);
//
//        mockedSecurityUtil.when(SecurityUtil::getCurrentAccount).thenReturn(mockAccount);
//        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);
//
//        Car car = new Car();
//        car.setId("1");
//        car.setDeposit(1000L);
//
//        Wallet wallet = new Wallet();
//        wallet.setId(accountId);
//        wallet.setBalance(500L);
//
//        when(carRepository.findById("1")).thenReturn(Optional.of(car));
//        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
//        when(carService.isCarAvailable(car.getId(), CreateBookingRequest.getPickUpTime(), CreateBookingRequest.getDropOffTime()))
//                .thenReturn(true);
//        when(redisUtil.generateBookingNumber()).thenReturn("B123");
//
//        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
//            CreateBookingRequest request = invocation.getArgument(0);
//            Booking mappedBooking = new Booking();
//            mappedBooking.setPickUpTime(request.getPickUpTime());
//            mappedBooking.setDropOffTime(request.getDropOffTime());
//            mappedBooking.setPaymentType(request.getPaymentType());
//            mappedBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
//            mappedBooking.setCar(car);
//            return mappedBooking;
//        });
//
//        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
//            Booking booking = invocation.getArgument(0);
//            BookingResponse response = new BookingResponse();
//            response.setBookingNumber("B123");
//            response.setCarId(booking.getCar().getId());
//            response.setStatus(booking.getStatus());
//            response.setPickUpTime(booking.getPickUpTime());
//            response.setDropOffTime(booking.getDropOffTime());
//            response.setTotalPrice(0L);
//            response.setDeposit(booking.getCar().getDeposit());
//            response.setPaymentType(EPaymentType.WALLET);
//            response.setDriverDrivingLicenseUrl("dummyUrl");
//            return response;
//        });
//
//        // When
//        BookingResponse response = bookingService.createBooking(CreateBookingRequest);
//
//        // Then
//        assertEquals(EBookingStatus.PENDING_DEPOSIT, response.getStatus());
//        assertEquals(500L, wallet.getBalance(), "Số dư ví không bị thay đổi vì chưa đủ tiền");
//        assertEquals(pickUpTime, response.getPickUpTime());
//        assertEquals(dropOffTime, response.getDropOffTime());
//
//        verify(transactionService, never()).payDeposit(any(), anyLong(), any());
//
//        verify(walletRepository, never()).save(any());
//    }

    @Test
    void createBooking_WhenPaymentByCashOrBankTransfer_ShouldSetStatusPendingDeposit() throws MessagingException {

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

//    @Test
//    void createBooking_ProfileIncomplete2_ShouldThrowException() {
//        String accountId = "user123";
//
//
//        CreateBookingRequest CreateBookingRequest = new CreateBookingRequest();
//        CreateBookingRequest.setCarId("car123");
//        CreateBookingRequest.setPaymentType(EPaymentType.WALLET);
//        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
//        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
//        CreateBookingRequest.setPickUpTime(mockPickUpTime);
//        CreateBookingRequest.setDropOffTime(mockDropOffTime);
//
//
//        Account mockAccount = new Account();
//        mockAccount.setId(accountId);
//
//
//        UserProfile mockProfile = new UserProfile();
//        mockProfile.setFullName("test");
//        mockProfile.setDob(null);
//        mockProfile.setNationalId("1234567890");
//        mockProfile.setPhoneNumber("0886980035");
//        mockProfile.setCityProvince("Tỉnh Hà Giang");
//        mockProfile.setDistrict("Thành phố Hà Giang");
//        mockProfile.setWard("Phường Quang Trung");
//        mockProfile.setHouseNumberStreet("211, Trần Duy Hưng");
//        mockProfile.setDrivingLicenseUri("test.jpg");
//
//        mockAccount.setProfile(mockProfile);
//
//
//        when(SecurityUtil.getCurrentAccount()).thenReturn(mockAccount);
//
//
//        AppException exception = assertThrows(AppException.class, () -> {
//            bookingService.createBooking(CreateBookingRequest);
//        });
//
//        assertEquals(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE, exception.getErrorCode());
//    }

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
}