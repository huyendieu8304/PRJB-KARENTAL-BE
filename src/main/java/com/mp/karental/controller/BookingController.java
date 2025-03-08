package com.mp.karental.controller;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.BookingResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    ApiResponse<BookingResponse> createBooking(@ModelAttribute BookingRequest bookingRequest,
                                               @RequestParam("carId") String carId) throws Exception {
        log.info("create booking {}", bookingRequest);
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(bookingRequest, carId))
                .build();
    }
}
