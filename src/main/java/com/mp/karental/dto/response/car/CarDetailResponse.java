package com.mp.karental.dto.response.car;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO representing the detailed information of a car.
 * Used to provide full car details in the car rental system.
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
public class CarDetailResponse {
    String licensePlate;
    String brand;
    String model;
    String status;
    String color;
    int numberOfSeats;
    int productionYear;
    float mileage;
    float fuelConsumption;
    long basePrice;
    long deposit;
    String address;
    String description;
    String additionalFunction;
    String termOfUse;
    boolean isAutomatic;
    boolean isGasoline;

    //documents
    String registrationPaperUrl;
    boolean registrationPaperIsVerified;
    String certificateOfInspectionUrl;
    boolean certificateOfInspectionIsVerified;
    String insuranceUrl;
    boolean insuranceIsVerified;

    //car image
    String carImageFront;
    String carImageBack;
    String carImageLeft;
    String carImageRight;

    long noOfRides;
    boolean isBooked;
    boolean isAvailable;

}
