package com.mp.karental.controller;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.*;
import com.mp.karental.service.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class BookingController {
    BookingService bookingService;

    @PostMapping(value = "/customer/createBook", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> createBooking(@ModelAttribute @Valid BookingRequest bookingRequest,
                                               @RequestParam("carId") String carId) throws Exception {
        log.info("create booking {}", bookingRequest);
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(bookingRequest, carId))
                .build();
    }

    /**
     * API endpoint to retrieve the list of bookings for the current customer.
     *
     * @param page the page number (default is 0)
     * @param size the number of records per page (default is 10)
     * @param sort sorting field and direction in the format "field,DIRECTION" (default is "productionYear,DESC")
     * @return a paginated list of bookings wrapped in `ApiResponse<Page<BookingThumbnailResponse>>`
     */
    @GetMapping("/customer/my-bookings")
    public ApiResponse<Page<BookingThumbnailResponse>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productionYear,DESC") String sort) {

        Page<BookingThumbnailResponse> bookings = bookingService.getBookingsByUserId(page, size, sort);
        return ApiResponse.<Page<BookingThumbnailResponse>>builder()
                .data(bookings)
                .build();
    }

}
