package com.mp.karental.dto.response;

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


}
