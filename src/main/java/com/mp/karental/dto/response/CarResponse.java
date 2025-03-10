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
    String id;
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
    boolean registrationPaperUriIsVerified;
    String certificateOfInspectionUrl;
    boolean certificateOfInspectionUriIsVerified;
    String insuranceUrl;
    boolean insuranceUriIsVerified;

    //car image
    String carImageFrontUrl;
    String carImageBackUrl;
    String carImageLeftUrl;
    String carImageRightUrl;


}
