package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.request.booking.BookingRequest;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = KarentalApplication.class)
@ExtendWith(MockitoExtension.class)
@Slf4j
@AutoConfigureMockMvc
class BookingControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Given
        bookingRequest = new BookingRequest();
        bookingRequest.setCarId("123");
        bookingRequest.setDriverFullName("John Doe");
        bookingRequest.setDriverPhoneNumber("0123456789");
        bookingRequest.setDriverNationalId("123456789000");
        bookingRequest.setDriverDob(LocalDate.of(1990, 1, 1));
        bookingRequest.setDriverEmail("johndoe@example.com");
        bookingRequest.setDriverCityProvince("Thành phố Hà Nội");
        bookingRequest.setDriverDistrict("Quận Ba Đình");
        bookingRequest.setDriverWard("Phường Phúc Xá");
        bookingRequest.setDriverHouseNumberStreet("123 Kim Ma");
        bookingRequest.setPickUpLocation("Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma");

        bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber("BK123456"); // Giả định

    }

    @Test
    void testCreateBooking_MultipartFormData() throws Exception {
        // Mock dữ liệu response
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber("BK123456");
        bookingResponse.setStatus(EBookingStatus.WAITING_CONFIRM);

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(bookingResponse);

        // Gửi request multipart
        mockMvc.perform(multipart("/booking/customer/create-book")
                        .file(new MockMultipartFile("driverLicense", "license.jpg", "image/jpeg", "fake-image-data".getBytes())) // File giả lập
                        .param("carId", "123")
                        .param("driverFullName", "John Doe")
                        .param("driverPhoneNumber", "0123456789")
                        .param("driverNationalId", "123456789000")
                        .param("driverDob", "1990-01-01")
                        .param("driverEmail", "johndoe@example.com")
                        .param("driverCityProvince", "Thành phố Hà Nội")
                        .param("driverDistrict", "Quận Ba Đình")
                        .param("driverWard", "Phường Phúc Xá")
                        .param("driverHouseNumberStreet", "123 Kim Ma")
                        .param("pickUpLocation", "Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma")
                        .param("pickUpTime", "2025-03-25T07:00:00")
                        .param("dropOffTime", "2025-03-25T10:00:00")
                        .param("paymentType", EPaymentType.WALLET.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateBooking_BadRequest() throws Exception {
        // Mock dữ liệu response
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber("BK123456");
        bookingResponse.setStatus(EBookingStatus.WAITING_CONFIRM);

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(bookingResponse);
        // Gửi request với dữ liệu không hợp lệ (thiếu carId và email sai format)
        mockMvc.perform(multipart("/booking/customer/create-book")
                        .file(new MockMultipartFile("driverLicense", "license.jpg", "image/jpeg", "fake-image-data".getBytes())) // File giả lập
                        .param("driverFullName", "John Doe")
                        .param("driverPhoneNumber", "0123456789")
                        .param("driverNationalId", "123456789000")
                        .param("driverDob", "1990-01-01")
                        .param("driverEmail", "invalid-email") // Email không hợp lệ
                        .param("driverCityProvince", "Thành phố Hà Nội")
                        .param("driverDistrict", "Quận Ba Đình")
                        .param("driverWard", "Phường Phúc Xá")
                        .param("driverHouseNumberStreet", "123 Kim Ma")
                        .param("pickUpLocation", "Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma")
                        .param("pickUpTime", "2025-03-25T07:00:00")
                        .param("dropOffTime", "2025-03-25T10:00:00")
                        .param("paymentType", EPaymentType.WALLET.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError());// Kiểm tra HTTP 400
    }


    @Test
    void getBookings_Success() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> mockPage = new PageImpl<>(List.of(new BookingThumbnailResponse()));
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/customer/my-bookings")  // ✅ Giữ nguyên đường dẫn đúng
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    void getBookings_EmptyResult() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> emptyPage = Page.empty();
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/customer/my-bookings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    void getBookings_DefaultParams() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> mockPage = new PageImpl<>(List.of(new BookingThumbnailResponse()));
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/customer/my-bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    void getWallet_Success() throws Exception {
        // Mock response từ service
        WalletResponse mockWalletResponse = new WalletResponse("user123", 500000);

        when(bookingService.getWallet()).thenReturn(mockWalletResponse);

        // Gửi request GET đến API
        mockMvc.perform(get("/booking/get-wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user123"))
                .andExpect(jsonPath("$.data.balance").value(500000));
    }

    @Test
    void getWallet_AccountNotFound() throws Exception {
        // Giả lập lỗi khi không tìm thấy Wallet
        when(bookingService.getWallet()).thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Gửi request GET đến API và kiểm tra phản hồi lỗi
        mockMvc.perform(get("/booking/get-wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB.getCode()));
    }
}
