package com.mp.karental.service;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.BookingResponse;
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

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("user-123");
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void createBooking_Success() throws AppException {
        String accountId = "user-123";

        // Mock request với giá trị hợp lệ
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);

        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
        MultipartFile mockFile = mock(MultipartFile.class);
        bookingRequest.setDriverDrivingLicense(mockFile);
        bookingRequest.setPickUpTime(mockPickUpTime);
        bookingRequest.setDropOffTime(mockDropOffTime);

        // Mock Account
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        // Mock Wallet
        Wallet wallet = new Wallet();
        wallet.setId(accountId);
        wallet.setBalance(10000);

        // Mock Car
        Car car = new Car();
        car.setId("car123");
        car.setDeposit(5000);
        car.setBasePrice(2000);

        // Mock Booking
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

        // Mock repository và service
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(anyString(), any(), any())).thenReturn(true);
        when(bookingMapper.toBooking(any())).thenReturn(booking);
        when(redisUtil.generateBookingNumber()).thenReturn("BK123");
        when(bookingMapper.toBookingResponse(any())).thenReturn(new BookingResponse());

        // Gọi service
        BookingResponse response = bookingService.createBooking(bookingRequest);

        // Kiểm tra kết quả
        assertNotNull(response);
        verify(bookingRepository, times(1)).save(any());
        verify(walletRepository, times(1)).save(any());
    }


    @Test
    void createBooking_CarNotFound_ThrowsException() {
        // 🔥 Tạo request với thời gian hợp lệ
        BookingRequest request = new BookingRequest();
        request.setCarId("car123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(18).withMinute(0));

        // Mock SecurityUtil để trả về accountId cố định
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("user123");

        // 🔥 Mock repository trả về empty khi tìm car
        when(carRepository.findById("car123")).thenReturn(Optional.empty());

        // 🔥 Kiểm tra ngoại lệ
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        // 🔥 Kiểm tra mã lỗi và thông điệp
        assertEquals(ErrorCode.CAR_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void createBooking_WalletNotFound_ThrowsException() {
        // 🔥 Mock dữ liệu request hợp lệ
        BookingRequest request = new BookingRequest();
        request.setCarId("car123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(10));
        request.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(18));

        // Mock SecurityUtil để trả về accountId cố định
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("user123");

        // Mock carRepository trả về một chiếc xe hợp lệ
        Car car = new Car();
        car.setId("car123");
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        // 🔥 Mock walletRepository trả về empty (Ví không tồn tại)
        when(walletRepository.findById("user123")).thenReturn(Optional.empty());

        // 🔥 Kiểm tra ngoại lệ
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        // 🔥 Kiểm tra mã lỗi
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void createBooking_CarNotAvailable_ThrowsException() {
        // 🔥 Mock dữ liệu request hợp lệ
        BookingRequest request = new BookingRequest();
        request.setCarId("car123");
        request.setPickUpTime(LocalDateTime.now().plusDays(1).withHour(10));
        request.setDropOffTime(LocalDateTime.now().plusDays(2).withHour(18));

        // Mock SecurityUtil để trả về accountId cố định
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("user123");

        // Mock carRepository trả về một chiếc xe hợp lệ
        Car car = new Car();
        car.setId("car123");
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        // Mock walletRepository trả về ví hợp lệ
        Wallet wallet = new Wallet();
        wallet.setId("user123");
        wallet.setBalance(10000);
        when(walletRepository.findById("user123")).thenReturn(Optional.of(wallet));

        // 🔥 Mock carService để xe KHÔNG khả dụng
        when(carService.isCarAvailable("car123", request.getPickUpTime(), request.getDropOffTime()))
                .thenReturn(false);

        // 🔥 Kiểm tra ngoại lệ
        AppException exception = assertThrows(AppException.class, () -> bookingService.createBooking(request));

        // 🔥 Kiểm tra mã lỗi
        assertEquals(ErrorCode.CAR_NOT_AVAILABLE, exception.getErrorCode());
    }

    @Test
    void createBooking_WhenWalletHasEnoughBalance_ShouldSetStatusWaitingConfirm() {
        // Given
        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("1");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        bookingRequest.setPickUpTime(pickUpTime);
        bookingRequest.setDropOffTime(dropOffTime);

        Car car = new Car();
        car.setId("1");
        car.setDeposit(1000L);

        Wallet wallet = new Wallet();
        wallet.setBalance(5000L); // Đủ tiền

        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        // Mock bookingMapper.toBooking()
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

        // Mock bookingRepository.save()
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 🔥 Mock bookingMapper.toBookingResponse() đầy đủ hơn
        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setBookingNumber("B123");
            response.setCarId(booking.getCar().getId());
            response.setStatus(booking.getStatus());
            response.setPickUpTime(booking.getPickUpTime());
            response.setDropOffTime(booking.getDropOffTime());
            response.setTotalPrice(2000L); // Giá trị giả định
            response.setDeposit(booking.getCar().getDeposit());
            response.setPaymentType(EPaymentType.WALLET);
            response.setDriverDrivingLicenseUrl("dummyUrl"); // ✅ Tránh null
            return response;
        });

        // When
        BookingResponse response = bookingService.createBooking(bookingRequest);


        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, response.getStatus());
        assertEquals(4000L, wallet.getBalance()); // Đã trừ tiền từ ví
        assertEquals(pickUpTime, response.getPickUpTime()); // Kiểm tra thời gian đã được gán đúng
        assertEquals(dropOffTime, response.getDropOffTime());
    }

    @Test
    void createBooking_WhenWalletHasNotEnoughBalance_ShouldSetStatusPendingDeposit() {
        // Given
        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("1");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        bookingRequest.setPickUpTime(pickUpTime);
        bookingRequest.setDropOffTime(dropOffTime);

        Car car = new Car();
        car.setId("1");
        car.setDeposit(1000L);

        Wallet wallet = new Wallet();
        wallet.setBalance(500L); // Không đủ tiền

        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        // Mock bookingMapper.toBooking()
        when(bookingMapper.toBooking(any())).thenAnswer(invocation -> {
            BookingRequest request = invocation.getArgument(0);
            Booking mappedBooking = new Booking();
            mappedBooking.setPickUpTime(request.getPickUpTime());
            mappedBooking.setDropOffTime(request.getDropOffTime());
            mappedBooking.setPaymentType(request.getPaymentType());
            mappedBooking.setStatus(EBookingStatus.PENDING_DEPOSIT); // Do không đủ tiền
            mappedBooking.setCar(car);
            return mappedBooking;
        });

        // Mock bookingRepository.save()
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 🔥 Mock bookingMapper.toBookingResponse() để không bị null
        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setBookingNumber("B123");
            response.setCarId(booking.getCar().getId());
            response.setStatus(booking.getStatus());
            response.setPickUpTime(booking.getPickUpTime());
            response.setDropOffTime(booking.getDropOffTime());
            response.setTotalPrice(0L); // Không trừ tiền ngay
            response.setDeposit(booking.getCar().getDeposit());
            response.setPaymentType(EPaymentType.WALLET);
            response.setDriverDrivingLicenseUrl("dummyUrl"); // ✅ Đảm bảo không bị null
            return response;
        });

        // When
        BookingResponse response = bookingService.createBooking(bookingRequest);

        // Then
        assertEquals(EBookingStatus.PENDING_DEPOSIT, response.getStatus());
        assertEquals(500L, wallet.getBalance(), "Số dư ví không bị thay đổi vì chưa đủ tiền");
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());
    }

    @Test
    void createBooking_WhenPaymentByCashOrBankTransfer_ShouldSetStatusPendingDeposit() {
        // Given
        String accountId = "testAccountId";
        LocalDateTime pickUpTime = LocalDateTime.now();
        LocalDateTime dropOffTime = pickUpTime.plusDays(1);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("1");
        bookingRequest.setPaymentType(EPaymentType.CASH); // Hoặc BANK_TRANSFER
        bookingRequest.setPickUpTime(pickUpTime);
        bookingRequest.setDropOffTime(dropOffTime);

        Car car = new Car();
        car.setId("1");
        car.setDeposit(1000L);

        Wallet wallet = new Wallet();
        wallet.setBalance(5000L); // Không ảnh hưởng đến test này

        when(SecurityUtil.getCurrentAccountId()).thenReturn(accountId);
        when(carRepository.findById("1")).thenReturn(Optional.of(car));
        when(walletRepository.findById(accountId)).thenReturn(Optional.of(wallet));
        when(carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()))
                .thenReturn(true);
        when(redisUtil.generateBookingNumber()).thenReturn("B123");

        // Mock bookingMapper.toBooking()
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

        // Mock bookingRepository.save()
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 🔥 Mock bookingMapper.toBookingResponse() để tránh bị null
        when(bookingMapper.toBookingResponse(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponse response = new BookingResponse();
            response.setBookingNumber("B123");
            response.setCarId(booking.getCar().getId());
            response.setStatus(booking.getStatus());
            response.setPickUpTime(booking.getPickUpTime());
            response.setDropOffTime(booking.getDropOffTime());
            response.setTotalPrice(0L); // Chưa tính tiền ngay
            response.setDeposit(booking.getCar().getDeposit());
            response.setPaymentType(EPaymentType.CASH);
            response.setDriverDrivingLicenseUrl("dummyUrl"); // ✅ Tránh null
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
    void updateStatusBookings_ShouldCancelExpiredBookings() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Booking expiredBooking = new Booking();
        expiredBooking.setCreatedAt(now.minusHours(2)); // Quá 1 giờ
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
        wallet.setBalance(2000L); // Đủ tiền
        Account account = new Account();
        account.setId("testAccount");
        Booking pendingBooking = new Booking();
        pendingBooking.setDeposit(1000L);
        pendingBooking.setAccount(account);
        Car car = new Car();
        car.setId("1");
        pendingBooking.setCar(car);
        pendingBooking.setPickUpTime(now.plusHours(1));
        pendingBooking.setDropOffTime(now.plusHours(5));
        pendingBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        when(bookingRepository.findPendingDepositBookings(any())).thenReturn(List.of(pendingBooking));
        when(walletRepository.findById(anyString())).thenReturn(Optional.of(wallet));
        when(bookingRepository.findByCarIdAndStatusAndTimeOverlap(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, pendingBooking.getStatus());
        assertEquals(1000L, wallet.getBalance()); // Giảm số dư
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

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, confirmedBooking.getStatus());
        assertEquals(EBookingStatus.CANCELLED, overlappingBooking.getStatus()); // Hủy booking trùng
        verify(bookingRepository).saveAndFlush(overlappingBooking);
    }

    @Test
    void updateStatusBookings_ShouldNotChangeStatusIfWalletBalanceIsNotEnough() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Wallet wallet = new Wallet();
        wallet.setBalance(500L); // Không đủ tiền
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
        assertEquals(EBookingStatus.PENDING_DEPOSIT, pendingBooking.getStatus()); // Không đổi trạng thái
        verify(walletRepository, never()).save(wallet);
        verify(bookingRepository, never()).save(pendingBooking);
    }

//    @Test
//    void updateStatusBookings_ShouldCancelOverlappingBookings() {
//        // Given
//        LocalDateTime now = LocalDateTime.now();
//
//        Wallet wallet = new Wallet();
//        wallet.setBalance(2000L); // Đủ tiền để xác nhận booking
//        Account account = new Account();
//        account.setId("testAccount");
//        Car car = new Car();
//        car.setId("1");
//        Booking confirmedBooking = new Booking();
//        confirmedBooking.setDeposit(1000L);
//        confirmedBooking.setAccount(account);
//        confirmedBooking.setCar(car);
//        confirmedBooking.setPickUpTime(now.plusHours(1));
//        confirmedBooking.setDropOffTime(now.plusHours(5));
//        confirmedBooking.setStatus(EBookingStatus.PENDING_DEPOSIT);
//
//        Booking overlappingBooking1 = new Booking();
//        overlappingBooking1.setCar(car);
//        overlappingBooking1.setPickUpTime(now.plusHours(2));
//        overlappingBooking1.setDropOffTime(now.plusHours(6));
//        overlappingBooking1.setStatus(EBookingStatus.PENDING_DEPOSIT);
//
//        Booking overlappingBooking2 = new Booking();
//        overlappingBooking2.setCar(car);
//        overlappingBooking2.setPickUpTime(now.plusHours(3));
//        overlappingBooking2.setDropOffTime(now.plusHours(7));
//        overlappingBooking2.setStatus(EBookingStatus.PENDING_DEPOSIT);
//
//        List<Booking> overlappingBookings = List.of(overlappingBooking1, overlappingBooking2);
//
//        when(bookingRepository.findPendingDepositBookings(any())).thenReturn(List.of(confirmedBooking));
//        when(walletRepository.findById(anyString())).thenReturn(Optional.of(wallet));
//        when(bookingRepository.findByCarIdAndStatusAndTimeOverlap(any(), any(), any(), any()))
//                .thenReturn(overlappingBookings);
//
//        // When
//        bookingService.updateStatusBookings();
//
//        // Then
//        assertEquals(EBookingStatus.WAITING_CONFIRM, confirmedBooking.getStatus());
//        assertEquals(EBookingStatus.CANCELLED, overlappingBooking1.getStatus());
//        assertEquals(EBookingStatus.CANCELLED, overlappingBooking2.getStatus());
//
//        verify(bookingRepository, times(1)).save(confirmedBooking);
//        verify(bookingRepository, times(2)).saveAndFlush(any(Booking.class)); // Gọi đúng số lần
//    }



}


