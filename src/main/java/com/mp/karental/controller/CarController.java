package com.mp.karental.controller;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.service.CarService;
import com.mp.karental.service.ExcelService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class CarController {
    CarService carService;
    ExcelService excelService;

    @PostMapping(value = "/addCar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CarResponse> addNewCar(@ModelAttribute @Valid AddCarRequest request)
    {
        log.info("add new car {}", request);
        return ApiResponse.<CarResponse>builder()
                .data(carService.addNewCar(request))
                .build();

    }

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

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        return ResponseEntity.ok(excelService.getAllBrands());
    }
    @GetMapping("/models")
    public ResponseEntity<List<String>> getAllModels() {
        return ResponseEntity.ok(excelService.getAllModels());
    }

    @GetMapping("/models/{brand}")
    public ResponseEntity<List<String>> getModelsByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(excelService.getModelsByBrand(brand));
    }

    @GetMapping("/brands/{model}")
    public ResponseEntity<List<String>> getBrandsByModel(@PathVariable String model) {
        return ResponseEntity.ok(excelService.getBrandsByModel(model));
    }

    @GetMapping("/customer/view-detail")
    public ApiResponse<CarResponse> getCarDetail(@RequestParam String carId) {
        log.info("Fetching car details for ID: {}", carId);
        return ApiResponse.<CarResponse>builder()
                .data(carService.getCarDetail(carId))
                .build();
    }

}
