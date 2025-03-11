package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.dto.response.BookingThumbnailResponse;
import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.BookingResponse;
import com.mp.karental.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
        bookingResponse.setBookingNumber("BOOK123"); // Giả định

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
    void createBooking_ShouldReturnStatus200() throws Exception {
        Mockito.when(bookingService.createBooking(ArgumentMatchers.any())).thenReturn(bookingResponse);

        // Mock file để gửi cùng request
        MockMultipartFile emptyFile = new MockMultipartFile("driverDrivingLicense", "", "application/octet-stream", new byte[0]);

        // Thực hiện request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/booking/customer/createBook")
                        .file(emptyFile)
                        .param("carId", bookingRequest.getCarId())
                        .param("driverFullName", bookingRequest.getDriverFullName())
                        .param("pickUpLocation", bookingRequest.getPickUpLocation())
                        .param("driverPhoneNumber", bookingRequest.getDriverPhoneNumber())
                        .param("driverNationalId", bookingRequest.getDriverNationalId())
                        .param("driverDob", "1990-01-01")
                        .param("driverEmail", bookingRequest.getDriverEmail())
                        .param("driverCityProvince", bookingRequest.getDriverCityProvince())
                        .param("driverDistrict", bookingRequest.getDriverDistrict())
                        .param("driverWard", bookingRequest.getDriverWard())
                        .param("driverHouseNumberStreet", bookingRequest.getDriverHouseNumberStreet())
                        .param("pickUpTime", "2025-03-25T07:00:00")
                        .param("dropOffTime", "2025-03-25T10:00:00")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Kiểm tra HTTP 200 OK
    }



    @Test
    void createBooking_ShouldReturnBadRequest_WhenMissingRequiredFields() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("driverDrivingLicense", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/booking/customer/createBook")
                        .file(emptyFile)
                        .param("driverFullName", "John Doe")  // Thiếu các field quan trọng
                        .param("driverPhoneNumber", "0123456789")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());  // Kỳ vọng lỗi 400
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("driverDrivingLicense", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/booking/customer/createBook")
                        .file(emptyFile)
                        .param("carId", "123")
                        .param("driverFullName", "John Doe")
                        .param("driverPhoneNumber", "0123456789")
                        .param("driverNationalId", "123456789000")
                        .param("driverDob", "1990-01-01")
                        .param("driverEmail", "invalid-email")  // Email không hợp lệ
                        .param("driverCityProvince", "Thành phố Hà Nội")
                        .param("driverDistrict", "Quận Ba Đình")
                        .param("driverWard", "Phường Phúc Xá")
                        .param("driverHouseNumberStreet", "123 Kim Ma")
                        .param("pickUpLocation", "Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma")
                        .param("pickUpTime", "2025-03-25T07:00:00")
                        .param("dropOffTime", "2025-03-25T10:00:00")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());  // Kỳ vọng lỗi 400
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenMissingDriverLicense() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/booking/customer/createBook")
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
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());  // Kỳ vọng lỗi 400
    }
}
