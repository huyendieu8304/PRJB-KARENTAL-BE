package com.mp.karental.service;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.BookingResponse;
import com.mp.karental.dto.response.BookingThumbnailResponse;
import com.mp.karental.dto.response.WalletResponse;
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

        // Mock request
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCarId("car123");
        bookingRequest.setPaymentType(EPaymentType.WALLET);
        LocalDateTime mockPickUpTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0);
        LocalDateTime mockDropOffTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0).withSecond(0);
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

        // ✅ Mock `payDeposit()` để cập nhật số dư Wallet
        doAnswer(invocation -> {
            wallet.setBalance(wallet.getBalance() - car.getDeposit()); // Trừ tiền trong test
            return null;
        }).when(transactionService).payDeposit(accountId, car.getDeposit(), booking);

        // Gọi service
        BookingResponse response = bookingService.createBooking(bookingRequest);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5000, wallet.getBalance()); // Đảm bảo số dư giảm đúng
        verify(transactionService, times(1)).payDeposit(accountId, car.getDeposit(), booking);
        verify(walletRepository, times(1)).save(wallet); // Đảm bảo save() được gọi
        verify(bookingRepository, times(1)).save(any());
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

        // ✅ Mock transactionService.payDeposit() để cập nhật số dư ví
        doAnswer(invocation -> {
            wallet.setBalance(wallet.getBalance() - car.getDeposit()); // Trừ tiền
            return null;
        }).when(transactionService).payDeposit(eq(accountId), eq(car.getDeposit()), any());

        // When
        BookingResponse response = bookingService.createBooking(bookingRequest);

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, response.getStatus());
        assertEquals(4000L, wallet.getBalance()); // Kiểm tra tiền đã trừ đúng
        assertEquals(pickUpTime, response.getPickUpTime());
        assertEquals(dropOffTime, response.getDropOffTime());

        // ✅ Đảm bảo walletRepository.save(wallet) đã được gọi
        verify(walletRepository, atMostOnce()).save(any());
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

        // ✅ Mock transactionService để thực sự trừ tiền trong test
        doAnswer(invocation -> {
            String accId = invocation.getArgument(0);
            Long deposit = invocation.getArgument(1);
            Booking booking = invocation.getArgument(2);

            wallet.setBalance(wallet.getBalance() - deposit); // Trừ tiền từ ví
            booking.setStatus(EBookingStatus.WAITING_CONFIRM); // Cập nhật trạng thái

            return null;
        }).when(transactionService).payDeposit(account.getId(), pendingBooking.getDeposit(), pendingBooking);

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, pendingBooking.getStatus()); // ✅ Kiểm tra trạng thái đã cập nhật
        assertEquals(1000L, wallet.getBalance()); // ✅ Kiểm tra số dư đã trừ đúng

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

        // ✅ Mock để cập nhật trạng thái overlappingBooking khi bị hủy
        doAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setStatus(EBookingStatus.CANCELLED);
            return null;
        }).when(bookingRepository).saveAndFlush(overlappingBooking);

        // When
        bookingService.updateStatusBookings();

        // Then
        assertEquals(EBookingStatus.WAITING_CONFIRM, confirmedBooking.getStatus()); // ✅ Booking chính được xác nhận
        assertEquals(EBookingStatus.CANCELLED, overlappingBooking.getStatus()); // ✅ Booking trùng bị hủy

        verify(bookingRepository).saveAndFlush(overlappingBooking); // ✅ Đảm bảo hàm được gọi
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

        // 🔥 Thêm Car để tránh lỗi
        Car car = new Car();
        car.setCarImageFront("car_front.jpg");
        booking.setCar(car);

        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking));

        BookingThumbnailResponse responseMock = new BookingThumbnailResponse();
        responseMock.setNumberOfDay(3);
        responseMock.setTotalPrice(300);

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(bookingsPage);
        when(bookingMapper.toBookingThumbnailResponse(any())).thenReturn(responseMock);
        when(fileService.getFileUrl(anyString())).thenReturn("http://example.com/image.jpg");

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
        int page = -1; // Không hợp lệ, sẽ được reset về 0
        int size = 10;
        String sort = "createdAt,DESC";

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);

        // Verify page đã reset về 0
        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getPageNumber() == 0
        ));
    }

    @Test
    void getBookingsByUserId_InvalidSize_ShouldResetToDefault() {
        // Arrange
        int page = 0;
        int size = 200; // Quá 100, sẽ bị reset về 10
        String sort = "createdAt,DESC";

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);

        // Verify size đã reset về 10
        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getPageSize() == 10
        ));
    }

    @Test
    void getBookingsByUserId_InvalidSort_ShouldUseDefault() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "invalidSort,DESC"; // Trường không hợp lệ -> dùng default (createdAt)

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);

        // Verify sorting giữ nguyên mặc định
        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getSort().equals(Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @Test
    void getBookingsByUserId_SortByBasePrice() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "basePrice,ASC"; // Sắp xếp theo basePrice

        when(bookingRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        bookingService.getBookingsByUserId(page, size, sort);

        // Verify sorting là basePrice ASC
        verify(bookingRepository).findByAccountId(eq(accountId), argThat(pageable ->
                pageable.getSort().equals(Sort.by(Sort.Direction.ASC, "basePrice"))
        ));
    }
    @Test
    void getWallet_Success() {
        // Giả lập accountId từ SecurityUtil
        String mockAccountId = "user123";
        when(SecurityUtil.getCurrentAccountId()).thenReturn(mockAccountId);

        // Giả lập wallet trong DB
        Wallet mockWallet = new Wallet();
        mockWallet.setId(mockAccountId);
        mockWallet.setBalance(500000);

        when(walletRepository.findById(mockAccountId)).thenReturn(Optional.of(mockWallet));

        // Gọi hàm
        WalletResponse response = bookingService.getWallet();

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(mockAccountId, response.getId());
        assertEquals(500000, response.getBalance());
    }

    @Test
    void getWallet_AccountNotFound() {
        // Giả lập accountId từ SecurityUtil
        String mockAccountId = "user123";
        when(SecurityUtil.getCurrentAccountId()).thenReturn(mockAccountId);

        // Giả lập trường hợp không tìm thấy Wallet
        when(walletRepository.findById(mockAccountId)).thenReturn(Optional.empty());

        // Kiểm tra xem có ném AppException không
        assertThrows(AppException.class, () -> bookingService.getWallet());
    }

}
