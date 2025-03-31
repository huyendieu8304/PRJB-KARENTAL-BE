package com.mp.karental.controller;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.car.AddCarRequest;
import com.mp.karental.dto.request.car.CarDetailRequest;
import com.mp.karental.dto.request.car.EditCarRequest;
import com.mp.karental.dto.request.car.SearchCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.car.CarDetailResponse;
import com.mp.karental.dto.response.car.CarDocumentsResponse;
import com.mp.karental.dto.response.car.CarResponse;
import com.mp.karental.dto.response.car.CarThumbnailResponse;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * REST controller for handling car-related operations.
 * <p>
 * This controller provides endpoints for user management functionalities,
 * including add, edit, view list of cars.
 * </p>
 *
 * @author QuangPM20
 *
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/car", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@Tag(name = "Car", description = "API for managing car")
public class CarController {
    CarService carService;

    /**
     * Handles the addition of a new car.
     *
     * @param request The request object containing car details.
     * @return ApiResponse containing the newly added car details.
     */
    @PostMapping(value = "/car-owner/add-car", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CarResponse> addNewCar(@ModelAttribute @Valid AddCarRequest request) {
        log.info("add new car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.addNewCar(request))
                .build();

    }

    /**
     * Handles editing an existing car.
     *
     * @param request The request object containing updated car details.
     * @param carId   The unique identifier of the car to be updated.
     * @return ApiResponse containing the updated car details.
     */
    @PutMapping(value = "/car-owner/edit-car/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CarResponse> editCar(@ModelAttribute @Valid EditCarRequest request, @PathVariable String carId) {
        log.info("edit car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.editCar(request, carId))
                .build();
    }

    /**
     * Handles get an existing car by id.
     *
     * @param carId The unique identifier of the car to be updated.
     * @return ApiResponse containing the get car details.
     */
    @Operation(
            summary = "View car detail",
            description = "Car owner can view their car detail.",
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
                                                    schema = @Schema(type = "object", implementation = CarResponse.class)
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
    @GetMapping("/car-owner/{carId}")
    public ApiResponse<CarResponse> getCarById(@PathVariable String carId) {
        return ApiResponse.<CarResponse>builder()
                .data(carService.getCarById(carId))
                .build();
    }

    /**
     * Retrieves car details including booking status within a specified date range.
     *
     * @return ApiResponse<CarDetailResponse> containing car details and booking status.
     */
    @Operation(
            summary = "View car detail",
            description = "Customer can view car detail.",
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
                                                    schema = @Schema(type = "object", implementation = CarDetailResponse.class)
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
                                    | 2025 | Invalid date range. Pick-up date must be before drop-off date.|
                                    | 3007 | The car is not exist in the system.|

                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 3008 | This car has not been verified and cannot be viewed.|
                                    
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @GetMapping("/customer/car-detail")
    public ApiResponse<CarDetailResponse> getCarDetail(
            @RequestParam String carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickUpTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropOffTime) {

        CarDetailRequest request = CarDetailRequest.builder()
                .carId(carId)
                .pickUpTime(pickUpTime)
                .dropOffTime(dropOffTime)
                .build();

        return ApiResponse.<CarDetailResponse>builder()
                .data(carService.getCarDetail(request))
                .build();
    }


    /**
     * Retrieves a paginated list of cars owned by the authenticated user.
     *
     * @param page The page number to retrieve (default is 0).
     * @param size The number of items per page (default is 10).
     * @param sort The sorting criteria in the format "field,order" (default is "productionYear,DESC").
     * @return ApiResponse containing a paginated list of car thumbnails.
     */
    @Operation(
            summary = "View list car",
            description = "Car owner can view their list car.",
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
                                                    schema = @Schema(type = "object", implementation = CarThumbnailResponse.class)
                                            )
                                    }
                            )
                    ),
            }
    )
    @GetMapping("/car-owner/my-cars")
    public ApiResponse<Page<CarThumbnailResponse>> getCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productionYear,DESC") String sort) {
        log.info("controller - getCars");
        Page<CarThumbnailResponse> cars = carService.getCarsByUserId(page, size, sort);
        return ApiResponse.<Page<CarThumbnailResponse>>builder()
                .data(cars)
                .build();
    }

    /**
     * API endpoint for searching cars based on address, pickup time, drop-off time, and sorting criteria.
     *
     * @param address     The address where the car is needed.
     * @param pickUpTime  The requested pickup time (ISO-8601 format).
     * @param dropOffTime The requested drop-off time (ISO-8601 format).
     * @param page        The page number for pagination (default: 0).
     * @param size        The number of cars per page (default: 10).
     * @param sort        The sorting criteria in the format "field,direction" (default: "productionYear,desc").
     * @return A response entity containing a paginated list of available cars.
     */
    @Operation(
            summary = "Search Car",
            description = "Customer can search car.",
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
                                                    schema = @Schema(type = "object", implementation = CarThumbnailResponse.class)
                                            )
                                    }
                            )
                    ),

            }
    )
    @GetMapping("/customer/search-car")
    public ApiResponse<Page<CarThumbnailResponse>> searchCars(
            @RequestParam String address,
            @RequestParam String pickUpTime,
            @RequestParam String dropOffTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productionYear,desc") String sort) {

        try {
            // Parse the pickup and drop-off time from String to LocalDateTime
            LocalDateTime realPickUpTime = LocalDateTime.parse(pickUpTime.trim());
            LocalDateTime realDropOffTime = LocalDateTime.parse(dropOffTime.trim());

            // Create a request DTO to hold the search parameters
            SearchCarRequest request = new SearchCarRequest(realPickUpTime, realDropOffTime, address);

            // Call service to fetch the list of available cars based on the criteria
            Page<CarThumbnailResponse> cars = carService.searchCars(request, page, size, sort);

            // Return a successful API response
            return ApiResponse.<Page<CarThumbnailResponse>>builder()
                    .data(cars)
                    .build();
        } catch (DateTimeParseException e) {
            // If the date format is incorrect, throw an AppException with the predefined error code
            throw new AppException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }

    /**
     * Retrieves a paginated list of all cars in the system for operators.
     * The list is sorted by updateAt in descending order, with NOT_VERIFIED cars appearing first.
     *
     * @param page   The page number (default = 0).
     * @param size   The number of records per page (default = 10).
     * @param sort   Sorting criteria (default = "updatedAt,desc").
     * @param status The status filter (optional).
     * @return A paginated list of cars.
     */
    @Operation(
            summary = "View list all car",
            description = "Operator can view list all car.",
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
                                                    schema = @Schema(type = "object", implementation = CarThumbnailResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping(value = "/operator/list")
    public ApiResponse<Page<CarThumbnailResponse>> getCarListForOperator(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) ECarStatus status) {
        log.info("Fetching car list for operator with filters - Page: {}, Size: {}, Sort: {}, Status: {}", page, size, sort, status);
        Page<CarThumbnailResponse> carList = carService.getAllCarsForOperator(page, size, sort, status);
        return ApiResponse.<Page<CarThumbnailResponse>>builder()
                .data(carList)
                .build();
    }


    /**
     * Retrieves the document details of a specific car.
     *
     * <p>This API endpoint fetches a car's document information using its unique ID.
     * The response includes document URLs and verification statuses.</p>
     *
     * @param carId The unique identifier of the car.
     * @return An {@link ApiResponse} containing the car's document details.
     */
    @Operation(
            summary = "View document car",
            description = "Operator can view document of car owner.",
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
                                                    schema = @Schema(type = "object", implementation = CarDocumentsResponse.class)
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

                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @GetMapping(value = "/operator/documents/{carId}")
    public ApiResponse<CarDocumentsResponse> getCarDocuments(@PathVariable String carId) {
        return ApiResponse.<CarDocumentsResponse>builder()
                .data(carService.getCarDocuments(carId))
                .build();
    }

    /**
     * Verifies a car by its ID.
     *
     * <p>This API is used by operators to verify a car, changing its status to VERIFIED.</p>
     *
     * @param carId The unique identifier of the car to be verified.
     * @return CarResponse containing the updated car details after verification.
     */
    @Operation(
            summary = "Verify car",
            description = "Operator verifies a car that is currently in NOT_VERIFIED status.",
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
                                                    schema = @Schema(type = "string", example = "Car has been verified successfully.")
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                    Bad request
                    |code  | message |
                    |------|--------------------------------------|
                    | 3007 | The car is not exist in the system. |
                    | 2024 | Car status must be NOT_VERIFIED. |
                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @PutMapping(value = "/operator/verify/{carId}")
    public ApiResponse<String> verifyCar(@PathVariable String carId) {
        return ApiResponse.<String>builder()
                .data(carService.verifyCar(carId))
                .build();
    }


}
