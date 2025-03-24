package com.mp.karental.dto.response.car;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO (Data Transfer Object) representing a summary of a car.
 * Used to display a list of cars in the car rental system.
 *
 * @author AnhHP9
 *
 * @version 1.0
 */
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
    private long basePrice;
    private String address;
    private String carImageFront;
    private String carImageRight;
    private String carImageLeft;
    private String carImageBack;
    long noOfRides;
    private double averageRatingByCar;
}
