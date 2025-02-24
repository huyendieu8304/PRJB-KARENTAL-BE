package com.mp.karental.controller;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.service.CarService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CarController {
    CarService carService;

    @PostMapping(value = "/addCar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CarResponse> addNewCar(@ModelAttribute AddCarRequest request)
    {
        log.info("add new car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.addNewCar(request))
                .build();

    }

    @GetMapping("/my-cars")
    public ApiResponse<ViewMyCarResponse> getCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productionYear,DESC") String sort) {
        ViewMyCarResponse cars = carService.getCarsByUserId(page, size, sort);
        return ApiResponse.<ViewMyCarResponse>builder()
                .data(cars)
                .build();
    }





}
