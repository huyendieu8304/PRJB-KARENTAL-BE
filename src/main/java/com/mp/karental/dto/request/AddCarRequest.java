package com.mp.karental.dto.request;

import com.mp.karental.validation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents the request payload for add a new car.
 * <p>
 * This class encapsulates the necessary data required to create a new car,
 * including car information.
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
@ValidBrandModel(message = "INVALID_BRAND_MODEL")
public class AddCarRequest {
    @RequiredField(fieldName = "License plate")
    //form license plate:(11-99)(A-Z)-(000-999).(00-99)
    @Pattern(regexp = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$", message = "INVALID_LICENSE")
    @UniqueLicensePlate(message = "NOT_UNIQUE_LICENSE")
    String licensePlate;

    @RequiredField(fieldName = "Brand of the car")
    @ValidBrand(message = "INVALID_BRAND")
    String brand;

    @RequiredField(fieldName = "Car model")
    @ValidModel(message = "INVALID_MODEL")
    String model;

    @RequiredField(fieldName = "Color of the car")
    @ValidColor(message = "INVALID_COLOR")
    String color;

    @ValidNumberOfSeats(message = "INVALID_NUMBER_OF_SEAT")
    int numberOfSeats;

    @ValidProductionYear(message = "INVALID_PRODUCTION_YEAR")
    int productionYear;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Mileage that the car has go")
    float mileage;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    float fuelConsumption;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Car's base price")
    long basePrice;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Deposit value")
    long deposit;

    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    String address;

    String description;

    @ValidAdditionalFunction(message = "INVALID_ADDITIONAL_FUNTION")
    String additionalFunction;

    String termOfUse;

    @RequiredField(fieldName = "Transmission type")
    boolean isAutomatic;

    @RequiredField(fieldName = "Fuel type")
    boolean isGasoline;

    //  MultipartFile**
    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @RequiredField(fieldName = "Registration paper")
    MultipartFile registrationPaper;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @RequiredField(fieldName = "Certificate of inspection")
    MultipartFile certificateOfInspection;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @RequiredField(fieldName = "Insurance")
    MultipartFile insurance;

    @RequiredField(fieldName = "Car's front side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageFront;

    @RequiredField(fieldName = "Car's back side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageBack;

    @RequiredField(fieldName = "Car's left side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageLeft;

    @RequiredField(fieldName = "Car's right side image")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageRight;

}
