package com.mp.karental.controller;

import com.mp.karental.dto.response.BookingThumbnailResponse;
import com.mp.karental.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @Test
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    void getBookings_Success() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> mockPage = new PageImpl<>(List.of(new BookingThumbnailResponse()));
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/customer/my-bookings")
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
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    void getBookings_EmptyResult() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> emptyPage = Page.empty();
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/customer/my-bookings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    void getBookings_DefaultParams() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> mockPage = new PageImpl<>(List.of(new BookingThumbnailResponse()));
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/customer/my-bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }
}
