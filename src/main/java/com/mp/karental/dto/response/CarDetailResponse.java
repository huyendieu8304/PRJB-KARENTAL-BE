package com.mp.karental.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    int basePrice;
    int deposit;
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

}
