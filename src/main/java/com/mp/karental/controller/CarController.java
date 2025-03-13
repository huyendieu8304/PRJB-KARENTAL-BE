package com.mp.karental.controller;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.CarDetailRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.request.SearchCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.service.CarService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

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
@RequestMapping("/car")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class CarController {
    CarService carService;

    /**
     * Handles the addition of a new car.
     *
     * @param request The request object containing car details.
     * @return ApiResponse containing the newly added car details.
     */
    @PostMapping(value = "/car-owner/add-car", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CarResponse> addNewCar(@ModelAttribute @Valid AddCarRequest request) {
        log.info("add new car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.addNewCar(request))
                .build();

    }

    /**
     * Handles editing an existing car.
     *
     * @param request The request object containing updated car details.
     * @param carId The unique identifier of the car to be updated.
     * @return ApiResponse containing the updated car details.
     */
    @PutMapping(value = "/car-owner/edit-car/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CarResponse> editCar(@ModelAttribute @Valid EditCarRequest request, @PathVariable String carId) {
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
