package com.mp.karental.controller;

import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.*;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.service.BookingService;
import jakarta.mail.MessagingException;
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

    BookingService bookingService;  // Service layer dependency for handling business logic

    /**
     * Handles the customer booking request.
     * This endpoint allows customers to create a new booking request with car details.
     *
     * @param bookingRequest The booking request payload, validated using @Valid
     * @return ApiResponse containing the created booking details
     */
    @PostMapping(value = "/customer/create-book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> createBooking(@ModelAttribute @Valid CreateBookingRequest bookingRequest) throws MessagingException {
        log.info("create booking {}", bookingRequest);
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(bookingRequest))
                .build();
    }

    @PutMapping(value = "/customer/edit-book/{bookingNumber}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> editBooking(@ModelAttribute @Valid EditBookingRequest editBookingRequest, @PathVariable String bookingNumber) throws MessagingException {
        log.info("edit booking {}", editBookingRequest);
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.editBooking(editBookingRequest, bookingNumber))
                .build();
    }

    /**
     * API endpoint to retrieve the list of bookings for the current customer.
     *
     * @param page the page number (default is 0)
     * @param size the number of records per page (default is 10)
     * @param sort sorting field and direction in the format "field,DIRECTION" (default is "createdAt,DESC")
     * @return a paginated list of bookings wrapped in `ApiResponse<Page<BookingThumbnailResponse>>`
     */
    @GetMapping("/customer/my-bookings")
    public ApiResponse<Page<BookingThumbnailResponse>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort) {

        Page<BookingThumbnailResponse> bookings = bookingService.getBookingsByUserId(page, size, sort);
        return ApiResponse.<Page<BookingThumbnailResponse>>builder()
                .data(bookings)
                .build();
    }

    /**
     * API endpoint to retrieve the wallet of account user login.
     *
     * @return a wallet in `ApiResponse<WalletResponse>`
     */
    @GetMapping("/get-wallet")
    public ApiResponse<WalletResponse> getWallet() {
        WalletResponse wallet = bookingService.getWallet();
        return ApiResponse.<WalletResponse>builder()
                .data(wallet)
                .build();
    }
    /**
     * Handles get an existing booking by booking number.
     *
     * @param bookingNumber The unique identifier of the booking to be updated.
     * @return ApiResponse containing the get booking details.
     */
    @GetMapping("/customer/{bookingNumber}")
    public ApiResponse<BookingResponse> getBookingByBookingNumber(@PathVariable String bookingNumber) {
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.getBookingDetailsByBookingNumber(bookingNumber))
                .build();
    }

    /**
     * Handles the cancellation of a booking by a customer.
     * This endpoint allows a customer to cancel their booking based on the provided booking number.
     * The cancellation process is managed by the {@code cancelBooking} method in {@code BookingService},
     * which handles refunds, updates the booking status, and sends necessary email notifications.
     *
     * @param bookingNumber The unique identifier of the booking to be canceled.
     * @return An {@link ApiResponse} containing the updated {@link BookingResponse} with the booking details.
     * @throws MessagingException If an error occurs while sending cancellation emails.
     */
    @PutMapping("/customer/cancel-booking/{bookingNumber}")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable String bookingNumber) throws MessagingException {
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.cancelBooking(bookingNumber))
                .build();
    }
}
