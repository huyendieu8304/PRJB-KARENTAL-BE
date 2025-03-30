package com.mp.karental.controller;

import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.dto.response.*;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.service.BookingService;
import com.mp.karental.dto.response.booking.BookingListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/booking", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@Tag(name = "Booking", description = "API for managing booking")
public class BookingController {

    BookingService bookingService;  // Service layer dependency for handling business logic

    @Operation(
            summary = "Create a booking",
            description = "This api allows customers to make a booking to rent a car ",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Success")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingResponse.class)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad request
                                    |code  | message |
                                    |------|-------------|
                                    | 3007 | The car is not exist in the system.|
                                    | 3003 | The account is not exist in the system.|
                                    | 3010 | The car is not available.|
                                    | 2031 | Driver's information is different from account holder, but the information is not fulfilled.|
                                    | 2004 | Invalid phone number.|
                                    | 2022 | National ID must contain exactly 12 digits.|
                                    | 2026 | Invalid booking time.|
                                    | 2028 | Invalid address component.|
                                    | 2002 | Please enter a valid email address.|
                                    """
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "503",
                            description = """
                                    Service_Unavailable
                                    |code  | message |
                                    |------|-------------|
                                    | 3001 | There was error occurred during uploading files. Please try again.|
                                    """
                    ),
            }
    )
    @PostMapping(value = "/customer/create-book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> createBooking(@ModelAttribute @Valid CreateBookingRequest bookingRequest) {
        log.info("create booking {}", bookingRequest);
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(bookingRequest))
                .build();
    }

    @Operation(
            summary = "Edit a booking",
            description = "This api allows customers to view/edit the booking details after renting a car",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Success")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingResponse.class)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad request
                                    |code  | message |
                                    |------|-------------|
                                    | 3018 | The booking is not exist in the system.|
                                    | 3019 | The booking cannot be edited as it is already in its current status.|
                                    | 2031 | Driver's information is different from account holder, but the information is not fulfilled.|
                                    | 2004 | Invalid phone number.|
                                    | 2022 | National ID must contain exactly 12 digits.|
                                    | 2002 | Please enter a valid email address.|
                                    """
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4014 | Can not view detail/edit booking of another account.|
                                    """
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "503",
                            description = """
                                    Service_Unavailable
                                    |code  | message |
                                    |------|-------------|
                                    | 3001 | There was error occurred during uploading files. Please try again.|
                                    """
                    ),
            }
    )
    @PutMapping(value = "/customer/edit-book/{bookingNumber}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> editBooking(@ModelAttribute @Valid EditBookingRequest editBookingRequest, @PathVariable @Parameter(description = "The booking number to be edited", example = "BK202410200001") String bookingNumber)  {
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
    public ApiResponse<BookingListResponse> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EBookingStatus status,
            @RequestParam(defaultValue = "updatedAt,DESC") String sort) {

        BookingListResponse response = bookingService.getBookingsOfCustomer(page, size, sort,
                (status != null) ? status.name() : null);

        return ApiResponse.<BookingListResponse>builder()
                .data(response)
                .build();
    }


    @Operation(
            summary = "Get all booking",
            description = "This api allows operators to view all the bookings in the system",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Success")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingListResponse.class)
                                            )
                                    }
                            )
                    ),
            }
    )
    @GetMapping("/operator/all-bookings")
    public ApiResponse<BookingListResponse> getBookingsForOperator(
            @Parameter(description = "Page number for pagination", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Size of the page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Filter by booking status", example = "CONFIRMED")
            @RequestParam(required = false) EBookingStatus status,

            @Parameter(description = "Sorting criteria in format: field,order", example = "updatedAt,DESC")
            @RequestParam(defaultValue = "updatedAt,DESC") String sort
    ) {

        BookingListResponse response = bookingService.getBookingsOfOperator(page, size, sort,
                (status != null) ? status.name() : null);

        return ApiResponse.<BookingListResponse>builder()
                .data(response)
                .build();
    }

    /**
     * API endpoint to retrieve the list of bookings for the current customer.
     *
     * @param page the page number (default is 0)
     * @param size the number of records per page (default is 10)
     * @param sort sorting field and direction in the format "field,DIRECTION" (default is "updatedAt,DESC")
     * @return a paginated list of bookings wrapped in `ApiResponse<Page<BookingThumbnailResponse>>`
     */
    @GetMapping("/car-owner/rentals")
    public ApiResponse<BookingListResponse> getBookingsForCarOwner(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EBookingStatus status,
            @RequestParam(defaultValue = "updatedAt,DESC") String sort) {

        BookingListResponse response = bookingService.getBookingsOfCarOwner(page, size, sort,
                (status != null) ? status.name() : null);

        return ApiResponse.<BookingListResponse>builder()
                .data(response)
                .build();
    }

    @Operation(
            summary = "Get balance of wallet",
            description = "This api allows customers to view the balance of their wallet",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Success")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = WalletResponse.class)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad request
                                    |code  | message |
                                    |------|-------------|
                                    | 3003 | The account is not exist in the system.|
                                    """
                    ),
            }
    )
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
     * Handles get an existing booking by booking number.
     *
     * @param bookingNumber The unique identifier of the booking to be updated.
     * @return ApiResponse containing the get booking details.
     */
    @GetMapping("/car-owner/{bookingNumber}")
    public ApiResponse<BookingResponse> getCustomerBookingDetails(@PathVariable String bookingNumber) {
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.getBookingDetailsByBookingNumber(bookingNumber))
                .build();
    }

    /**
     * API endpoint for car owners to confirm a booking.
     *
     * @param bookingNumber The unique booking number to be confirmed.
     * @return BookingResponse containing updated booking details.
     */
    @PutMapping("/car-owner/{bookingNumber}/confirm")
    public BookingResponse confirmBooking(@PathVariable String bookingNumber) {
        return bookingService.confirmBooking(bookingNumber);
    }

    /**
     * Handles the cancellation of a booking by a customer.
     * This endpoint allows a customer to cancel their booking based on the provided booking number.
     * The cancellation process is managed by the {@code cancelBooking} method in {@code BookingService},
     * which handles refunds, updates the booking status, and sends necessary email notifications.
     *
     * @param bookingNumber The unique identifier of the booking to be canceled.
     * @return An {@link ApiResponse} containing the updated {@link BookingResponse} with the booking details.
     */
    @PutMapping("/customer/cancel-booking/{bookingNumber}")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable String bookingNumber)  {
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.cancelBooking(bookingNumber))
                .build();
    }

    @PutMapping("/customer/confirm-pick-up/{bookingNumber}")
    public ApiResponse<BookingResponse> confirmPickUpBooking(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.confirmPickUp(bookingNumber))
                .build();
    }

    @PutMapping("/customer/return-car/{bookingNumber}")
    public ApiResponse<BookingResponse> returnCar(@PathVariable String bookingNumber) {
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.returnCar(bookingNumber))
                .build();
    }

    @PutMapping("/car-owner/confirm-early-return/{bookingNumber}")
    public ApiResponse<BookingResponse> confirmEarlyReturnCar(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.confirmEarlyReturnCar(bookingNumber))
                .build();
    }

    @PutMapping("/car-owner/reject-early-return/{bookingNumber}")
    public ApiResponse<BookingResponse> rejectEarlyReturnCar(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.rejectWaitingConfirmedEarlyReturnCarBooking(bookingNumber))
                .build();
    }

    @PutMapping("/car-owner/reject-booking/{bookingNumber}")
    public ApiResponse<BookingResponse> rejectBooking(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.rejectWaitingConfirmedBooking(bookingNumber))
                .build();
    }

    @PutMapping("/operator/confirm-deposit/{bookingNumber}")
    public ApiResponse<BookingResponse> confirmDeposit(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.confirmDeposit(bookingNumber))
                .build();
    }
    @PutMapping("/operator/reject-deposit/{bookingNumber}")
    public ApiResponse<BookingResponse> rejectDeposit(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.rejectDeposit(bookingNumber))
                .build();
    }
    @PutMapping("/customer/pay-deposit-again/{bookingNumber}")
    public ApiResponse<BookingResponse> payDepositAgain(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.payDepositAgain(bookingNumber))
                .build();
    }

    @PutMapping("/customer/pay-total-payment-again/{bookingNumber}")
    public ApiResponse<BookingResponse> payTotalPaymentAgain(@PathVariable String bookingNumber){
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.payTotalPaymentAgain(bookingNumber))
                .build();
    }

}
