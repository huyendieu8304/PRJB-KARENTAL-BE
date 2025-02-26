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
    private String carImageFrontUrl;
    private String carImageRightUrl;
    private String carImageLeftUrl;
    private String carImageBackUrl;


}
