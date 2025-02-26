package com.mp.karental.dto.response;

import com.mp.karental.entity.Car;
import com.mp.karental.service.FileService;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CarThumbnailResponse {
    private String id;
    private String brand;
    private String model;
    private int productionYear;
    private String status;
    private float mileage;
    private int basePrice;
    private String address;
    private String carImageFront;
    private String carImageRight;
    private String carImageLeft;
    private String carImageBack;

    public static CarThumbnailResponse fromCar(Car car, FileService fileService) {
        return CarThumbnailResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .productionYear(car.getProductionYear())
                .status(car.getStatus())
                .mileage(car.getMileage())
                .basePrice(car.getBasePrice())
//                .address(car.getAddress())
                .carImageFront(fileService.getFileUrl(car.getCarImageFront()))
                .carImageRight(fileService.getFileUrl(car.getCarImageRight()))
                .carImageLeft(fileService.getFileUrl(car.getCarImageLeft()))
                .carImageBack(fileService.getFileUrl(car.getCarImageBack()))
                .build();
    }
}
