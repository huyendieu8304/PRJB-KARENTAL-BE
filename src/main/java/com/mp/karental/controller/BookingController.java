package com.mp.karental.controller;

import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.dto.response.*;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.dto.response.user.UserResponse;
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

    /**
     * Handles the customer booking request.
     * This endpoint allows customers to create a new booking request with car details.
     *
     * @param bookingRequest The booking request payload, validated using @Valid
     * @return ApiResponse containing the created booking details
     */
    @PostMapping(value = "/customer/create-book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> createBooking(@ModelAttribute @Valid CreateBookingRequest bookingRequest) {
        log.info("create booking {}", bookingRequest);
        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(bookingRequest))
                .build();
    }

    @PutMapping(value = "/customer/edit-book/{bookingNumber}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookingResponse> editBooking(@ModelAttribute @Valid EditBookingRequest editBookingRequest, @PathVariable String bookingNumber)  {
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
    @Operation(
            summary = "View list bookings",
            description = "Customer view their list of booking.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (default = 0)", example = "0"),
                    @Parameter(name = "size", description = "Number of records per page (default = 10)", example = "10"),
                    @Parameter(name = "status", description = "Booking status filter", schema = @Schema(implementation = EBookingStatus.class)),
                    @Parameter(name = "sort", description = "Sorting field and direction (default = 'updatedAt,DESC')", example = "updatedAt,DESC")
            },
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
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingListResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
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

    /**
     * API endpoint to retrieve the list of bookings for the current customer.
     *
     * @param page the page number (default is 0)
     * @param size the number of records per page (default is 10)
     * @param sort sorting field and direction in the format "field,DIRECTION" (default is "updatedAt,DESC")
     * @return a paginated list of bookings wrapped in `ApiResponse<Page<BookingThumbnailResponse>>`
     */
    @Operation(
            summary = "View list rentals",
            description = "Car owner can view their list of rentals",
            parameters = {
                    @Parameter(name = "page", description = "Page number (default = 0)", example = "0"),
                    @Parameter(name = "size", description = "Number of records per page (default = 10)", example = "10"),
                    @Parameter(name = "status", description = "Booking status filter", schema = @Schema(implementation = EBookingStatus.class)),
                    @Parameter(name = "sort", description = "Sorting field and direction (default = 'updatedAt,DESC')", example = "updatedAt,DESC")
            },
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
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingListResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
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
    @Operation(
            summary = "View booking detail",
            description = "Customer can view their detail booking.",
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
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingResponse.class)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = """
                                    Not Found
                                    |code  | message |
                                    |------|-------------|
                                    | 3018 | The booking is not exist in the system.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4010 | Can not view detail/edit car of another account.|
                                    | 4014 | Can not view detail/edit car of another account.|
                                    
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
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
    @Operation(
            summary = "View booking detail",
            description = "Car owner can view their detail booking.",
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
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = BookingResponse.class)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = """
                                    Not Found
                                    |code  | message |
                                    |------|-------------|
                                    | 3018 | The booking is not exist in the system.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4010 | Can not view detail/edit car of another account.|
                                    | 4014 | Can not view detail/edit car of another account.|
                                    
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @GetMapping("/car-owner/{bookingNumber}")
    public ApiResponse<BookingResponse> getCarOwnerBookingDetails(@PathVariable String bookingNumber) {
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
    @Operation(
            summary = "Confirm booking",
            description = "Car owner can confirm customer's booking.",
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
                                                    schema = @Schema(type = "string", example = "Successful!")
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
                                    | 3020 | This booking cannot be confirmed due to its current status.|
                                    | 3021 | This booking has expired and cannot be confirmed.|
                                    
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = """
                                    Not Found
                                    |code  | message |
                                    |------|-------------|
                                    | 3018 | The booking is not exist in the system.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4010 | Can not view detail/edit car of another account.|
                                    
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
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

}
