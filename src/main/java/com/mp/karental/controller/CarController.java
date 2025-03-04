package com.mp.karental.controller;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.service.CarService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping(value = "/car-owner/addCar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    @PutMapping(value = "/car-owner/editCar/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CarResponse> editCar(@ModelAttribute @Valid EditCarRequest request, @PathVariable String carId) {
        log.info("edit car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.editCar(request, carId))
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
        Page<CarThumbnailResponse> cars = carService.getCarsByUserId(page, size, sort);
        return ApiResponse.<Page<CarThumbnailResponse>>builder()
                .data(cars)
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

    @GetMapping("/customer/view-detail")
    public ApiResponse<CarDetailResponse> getCarDetail(@RequestParam String carId) {
        log.info("Fetching car details for ID: {}", carId);
        return ApiResponse.<CarDetailResponse>builder()
                .data(carService.getCarDetail(carId))
                .build();
    }
}
