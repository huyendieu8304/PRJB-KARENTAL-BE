package com.mp.karental.service;

import com.mp.karental.dto.response.BookingThumbnailResponse;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Car;
import com.mp.karental.mapper.BookingMapper;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.service.BookingService;
import com.mp.karental.service.FileService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private BookingService bookingService;

    private String accountId;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setup() {
        accountId = "user123";
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    void getBookingsByUserId_Success() {
            // Arrange
            securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

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
}
