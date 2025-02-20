package com.mp.karental.controller;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.service.CarService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
}
