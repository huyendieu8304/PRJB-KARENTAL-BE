package com.mp.karental.controller;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.BookingResponse;
import com.mp.karental.service.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling booking-related operations.
 * <p>
 * This controller provides endpoints for user management functionalities,
 * including create booking.
 * </p>
 *
 * @author QuangPM20
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class BookingController {

    BookingService bookingService;  // Service layer dependency for handling business logic

    /**
     * Handles the customer booking request.
     * This endpoint allows customers to create a new booking request with car details.
     *
     * @param bookingRequest The booking request payload, validated using @Valid
     * @return ApiResponse containing the created booking details
     */
    @PostMapping(value = "/customer/createBook", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> createBooking(@ModelAttribute @Valid BookingRequest bookingRequest) {
        log.info("create booking {}", bookingRequest);  // Logs the booking request for debugging purposes

        // Calls the service layer to create the booking and wraps the response in an API response format
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(bookingRequest))
                .build();
    }
}
