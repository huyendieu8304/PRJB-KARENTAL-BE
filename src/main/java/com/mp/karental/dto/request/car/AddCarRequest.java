package com.mp.karental.dto.request.car;

import com.mp.karental.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents the request payload for adding a new car.
 * <p>
 * This class encapsulates the necessary data required to create a new car,
 * including car information.
 * </p>
 * @author QuangPM20
 * @version 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@ValidBrandModel(message = "INVALID_BRAND_MODEL")
@Schema(description = "Request payload for adding a new car.")
public class AddCarRequest {

    @Schema(description = "License plate number of the car", example = "30A-123.45")
    @RequiredField(fieldName = "License plate")
    @Pattern(regexp = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$", message = "INVALID_LICENSE")
    @UniqueLicensePlate(message = "NOT_UNIQUE_LICENSE")
    String licensePlate;

    @Schema(description = "Brand of the car", example = "Toyota")
    @RequiredField(fieldName = "Brand of the car")
    @ValidBrand(message = "INVALID_BRAND")
    String brand;

    @Schema(description = "Car model", example = "Camry")
    @RequiredField(fieldName = "Car model")
    @ValidModel(message = "INVALID_MODEL")
    String model;

    @Schema(description = "Color of the car", example = "Red")
    @RequiredField(fieldName = "Color of the car")
    @ValidColor(message = "INVALID_COLOR")
    String color;

    @Schema(description = "Number of seats in the car", example = "5")
    @ValidNumberOfSeats(message = "INVALID_NUMBER_OF_SEAT")
    int numberOfSeats;

    @Schema(description = "Year the car was manufactured", example = "2020")
    @ValidProductionYear(message = "INVALID_PRODUCTION_YEAR")
    int productionYear;

    @Schema(description = "Mileage the car has traveled (in km)", example = "15000")
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Mileage that the car has gone")
    float mileage;

    @Schema(description = "Fuel consumption of the car (liters per 100 km)", example = "7.5")
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    float fuelConsumption;

    @Schema(description = "Base price of the car rental", example = "500000")
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Car's base price")
    long basePrice;

    @Schema(description = "Deposit amount required for renting the car", example = "200000")
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Deposit value")
    long deposit;

    @Schema(description = "Address where the car is available", example = "123 Le Loi, Hanoi")
    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    String address;

    @Schema(description = "Additional description about the car", example = "Well-maintained, smoke-free car")
    String description;

    @Schema(description = "Additional functions or features of the car", example = "Sunroof, Bluetooth connectivity")
    @ValidAdditionalFunction(message = "INVALID_ADDITIONAL_FUNCTION")
    String additionalFunction;

    @Schema(description = "Terms of use for renting the car", example = "No smoking inside the car")
    String termOfUse;

    @Schema(description = "Indicates if the car has automatic transmission", example = "true")
    @RequiredField(fieldName = "Transmission type")
    boolean isAutomatic;

    @Schema(description = "Indicates if the car uses gasoline", example = "true")
    @RequiredField(fieldName = "Fuel type")
    boolean isGasoline;

    // MultipartFile (Car documents)
    @Schema(description = "Registration paper document file")
    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @RequiredField(fieldName = "Registration paper")
    MultipartFile registrationPaper;

    @Schema(description = "Certificate of inspection document file")
    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @RequiredField(fieldName = "Certificate of inspection")
    MultipartFile certificateOfInspection;

    @Schema(description = "Insurance document file")
    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @RequiredField(fieldName = "Insurance")
    MultipartFile insurance;

    // MultipartFile (Car images)
    @Schema(description = "Front-side image of the car")
    @RequiredField(fieldName = "Car's front side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageFront;

    @Schema(description = "Back-side image of the car")
    @RequiredField(fieldName = "Car's back side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageBack;

    @Schema(description = "Left-side image of the car")
    @RequiredField(fieldName = "Car's left side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageLeft;

    @Schema(description = "Right-side image of the car")
    @RequiredField(fieldName = "Car's right side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageRight;
}
