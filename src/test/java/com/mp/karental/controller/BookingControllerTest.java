package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.dto.response.BookingThumbnailResponse;
import com.mp.karental.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();  // ✅ Bỏ qua security khi test
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
}
