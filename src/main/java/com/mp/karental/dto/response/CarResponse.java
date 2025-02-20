package com.mp.karental.dto.response;

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
public class CarResponse {
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
    int reservationPrice;
    String address;
    String description;
    String additionalFunction;
    String termOfUse;
    boolean isAutomatic ;
    boolean isGasoline;

    //documents
    String registrationPaperUri;
    boolean registrationPaperUriIsVerified=true;
    String certificateOfInspectionUri;
    boolean certificateOfInspectionUriIsVerified=true;
    String insuranceUri;
    boolean insuranceUriIsVerified=true;

    //car image
    String carImageFront;
    String carImageBack;
    String carImageLeft;
    String carImageRight;
}
