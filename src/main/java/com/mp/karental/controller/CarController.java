package com.mp.karental.controller;

import com.mp.karental.dto.request.car.AddCarRequest;
import com.mp.karental.dto.request.car.CarDetailRequest;
import com.mp.karental.dto.request.car.EditCarRequest;
import com.mp.karental.dto.request.car.SearchCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.car.CarDetailResponse;
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
@RequestMapping(value = "/car", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@Tag(name = "Car", description = "API for managing car")
public class CarController {
    CarService carService;

    @Operation(
            summary = "Add a car",
            description = "This api allows car owners to add a car for rent",
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
                                                    schema = @Schema(type = "object")
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
                                    | 2000 | {fieldName} is required.|
                                    | 2007 | Invalid license plate format! Expected format: (11-99)(A-Z)-(000-999).(00-99).|
                                    | 2008 | License plate already existed. Please try another license plate.|
                                    | 2013 | Your brand were not predefined. Please try another brand.|
                                    | 2014 | Your model were not predefined. Please try another model.|
                                    | 2015 | Your brand-model were not matched. Please try again.|
                                    | 2009 | Your color were not predefined. Please try another color.|
                                    | 2012 | Invalid number of seats. Allowed values are 4, 5, or 7.|
                                    | 2011 | Your production year was not predefined. Please try another year.|
                                    | 2018 | This attribute must be >=0.|
                                    | 2019 | The address is invalid.|
                                    | 2010 | Your additional functions were not predefined. Please try another function.|
                                    | 2016 | Invalid file type. Accepted formats are .doc, .docx, .pdf, .jpeg, .jpg, .png.|
                                    | 2017 | Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "503",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 3001 | There was error occurred during uploading files. Please try again.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
            }
    )
    @PostMapping(value = "/car-owner/add-car", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CarResponse> addNewCar(@ModelAttribute @Valid AddCarRequest request) {
        log.info("add new car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.addNewCar(request))
                .build();

    }

    @Operation(
            summary = "Edit a car",
            description = "This api allows car owners to edit the information of a car",
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
                                                    schema = @Schema(type = "object")
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
                                    | 2024 | Allowed transitions: NOT_VERIFIED → STOPPED, STOPPED → NOT_VERIFIED, VERIFIED → STOPPED.|
                                    | 2018 | This attribute must be >=0.|
                                    | 2000 | {fieldName} is required.|
                                    | 2019 | The address is invalid.|
                                    | 2010 | Your additional functions were not predefined. Please try another function.|
                                    | 2017 | Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif.|
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
                                    | 3037 | The car cannot be stopped when has on-time booking.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "503",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 3001 | There was error occurred during uploading files. Please try again.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
            }
    )
    @PutMapping(value = "/car-owner/edit-car/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CarResponse> editCar(@ModelAttribute @Valid EditCarRequest request, @PathVariable String carId) {
        log.info("edit car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.editCar(request, carId))
                .build();
    }

    @Operation(
            summary = "Get car detail for car owner",
            description = "This api allows car owners to get the information of a car",
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
                                                    schema = @Schema(type = "object")
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
                    ),
            }
    )
    @GetMapping("/car-owner/{carId}")
    public ApiResponse<CarResponse> getCarById(@PathVariable String carId) {
        return ApiResponse.<CarResponse>builder()
                .data(carService.getCarById(carId))
                .build();
    }

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
     * @param address The address where the car is needed.
     * @param pickUpTime The requested pickup time (ISO-8601 format).
     * @param dropOffTime The requested drop-off time (ISO-8601 format).
     * @param page The page number for pagination (default: 0).
     * @param size The number of cars per page (default: 10).
     * @param sort The sorting criteria in the format "field,direction" (default: "productionYear,desc").
     * @return A response entity containing a paginated list of available cars.
     */
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

}
