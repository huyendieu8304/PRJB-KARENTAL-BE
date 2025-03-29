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
@Schema(name = "response.car.CarResponse", description = "Data of a car")
public class CarResponse {
    @Schema(description = "id of car", example = "car1")
    String id;
    @Schema(description = "license plate of car", example = "28F-125.13")
    String licensePlate;
    @Schema(description = "brand of car", example = "Toyota")
    String brand;
    @Schema(description = "model of car", example = "Camry")
    String model;
    @Schema(description = "status of car", example = "NOT_VERIFIED")
    String status;
    @Schema(description = "color of car", example = "Black")
    String color;
    @Schema(description = "number of seats of car", example = "4")
    int numberOfSeats;
    @Schema(description = "production year of car", example = "2000")
    int productionYear;
    @Schema(description = "mileage of car", example = "15000.0")
    float mileage;
    @Schema(description = "fuel consumption of car", example = "7.5")
    float fuelConsumption;
    @Schema(description = "base price of car", example = "1000000")
    long basePrice;
    @Schema(description = "deposit of car", example = "500000")
    long deposit;
    @Schema(description = "address of car", example = "Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung,211 Trần Duy Hưng")
    String address;
    @Schema(example = "xe toyota camry")
    String description;
    @Schema(example = "Sun Roof, Bluetooth")
    String additionalFunction;
    @Schema(example = "No smoking")
    String termOfUse;
    @Schema(example = "true")
    boolean isAutomatic;
    @Schema(example = "true")
    boolean isGasoline;

    //documents
    String registrationPaperUrl;
    @Schema(example = "false")
    boolean registrationPaperUriIsVerified;
    String certificateOfInspectionUrl;
    @Schema(example = "false")
    boolean certificateOfInspectionUriIsVerified;
    String insuranceUrl;
    @Schema(example = "false")
    boolean insuranceUriIsVerified;

    //car image
    String carImageFrontUrl;
    String carImageBackUrl;
    String carImageLeftUrl;
    String carImageRightUrl;

    double averageRatingByCar;

}
