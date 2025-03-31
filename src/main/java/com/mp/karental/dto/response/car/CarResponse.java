package com.mp.karental.dto.response.car;


import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response containing car details.")
public class CarResponse {
    @Schema(description = "Unique identifier of the car", example = "123e4567-e89b-12d3-a456-426614174000")
    String id;

    @Schema(description = "License plate of the car", example = "30A-123.45")
    String licensePlate;

    @Schema(description = "Brand of the car", example = "Toyota")
    String brand;

    @Schema(description = "Model of the car", example = "Camry")
    String model;

    @Schema(description = "Current status of the car", example = "AVAILABLE")
    String status;

    @Schema(description = "Color of the car", example = "Red")
    String color;

    @Schema(description = "Number of seats in the car", example = "5")
    int numberOfSeats;

    @Schema(description = "Year the car was manufactured", example = "2020")
    int productionYear;

    @Schema(description = "Current mileage of the car in kilometers", example = "15000.5")
    float mileage;

    @Schema(description = "Fuel consumption in liters per 100 km", example = "6.5")
    float fuelConsumption;

    @Schema(description = "Base rental price of the car per day", example = "500000")
    long basePrice;

    @Schema(description = "Deposit amount required for the car rental", example = "2000000")
    long deposit;

    @Schema(description = "Address where the car is available for pickup", example = "123 Main St, Hanoi, Vietnam")
    String address;

    @Schema(description = "Additional description of the car", example = "Well-maintained, good condition")
    String description;

    @Schema(description = "Additional functionalities of the car", example = "GPS, Sunroof")
    String additionalFunction;

    @Schema(description = "Terms of use for renting the car", example = "No smoking allowed, Fuel must be refilled before return")
    String termOfUse;

    @Schema(description = "Indicates if the car has an automatic transmission", example = "true")
    boolean isAutomatic;

    @Schema(description = "Indicates if the car runs on gasoline", example = "true")
    boolean isGasoline;

    // Documents
    @Schema(description = "URL of the car's registration paper", example = "https://example.com/registration.pdf")
    String registrationPaperUrl;

    @Schema(description = "Indicates whether the registration paper has been verified", example = "true")
    boolean registrationPaperUriIsVerified;

    @Schema(description = "URL of the car's certificate of inspection", example = "https://example.com/inspection.pdf")
    String certificateOfInspectionUrl;

    @Schema(description = "Indicates whether the certificate of inspection has been verified", example = "true")
    boolean certificateOfInspectionUriIsVerified;

    @Schema(description = "URL of the car's insurance document", example = "https://example.com/insurance.pdf")
    String insuranceUrl;

    @Schema(description = "Indicates whether the insurance document has been verified", example = "true")
    boolean insuranceUriIsVerified;

    // Car images
    @Schema(description = "URL of the car's front-side image", example = "https://example.com/car_front.jpg")
    String carImageFrontUrl;

    @Schema(description = "URL of the car's back-side image", example = "https://example.com/car_back.jpg")
    String carImageBackUrl;

    @Schema(description = "URL of the car's left-side image", example = "https://example.com/car_left.jpg")
    String carImageLeftUrl;

    @Schema(description = "URL of the car's right-side image", example = "https://example.com/car_right.jpg")
    String carImageRightUrl;

    @Schema(description = "Average rating of the car based on customer reviews", example = "4.5")
    double averageRatingByCar;
}
