package com.mp.karental.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the response payload for a car.
 * <p>
 * This class encapsulates car information that is returned to the client,
 * including car details.
 * </p>
 * @author QuangPM20
 *
 * @version 1.0
 */

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
    String address;
    String description;
    String additionalFunction;
    String termOfUse;
    boolean isAutomatic;
    boolean isGasoline;

    //documents
    String registrationPaperUri;
    boolean registrationPaperUriIsVerified;
    String certificateOfInspectionUri;
    boolean certificateOfInspectionUriIsVerified;
    String insuranceUri;
    boolean insuranceUriIsVerified;

    //car image
    String carImageFront;
    String carImageBack;
    String carImageLeft;
    String carImageRight;


}
