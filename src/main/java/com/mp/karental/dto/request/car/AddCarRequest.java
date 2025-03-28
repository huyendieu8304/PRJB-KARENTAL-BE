package com.mp.karental.dto.request.car;

import com.mp.karental.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "request.car.AddCarRequest", description = "DTO contain necessary information to add a car")
public class AddCarRequest {
    @RequiredField(fieldName = "License plate")
    //form license plate:(11-99)(A-Z)-(000-999).(00-99)
    @Pattern(regexp = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$", message = "INVALID_LICENSE")
    @UniqueLicensePlate(message = "NOT_UNIQUE_LICENSE")
    @Schema(example = "28F-125.13",pattern = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$")
    String licensePlate;

    @RequiredField(fieldName = "Brand of the car")
    @ValidBrand(message = "INVALID_BRAND")
    @Schema(example = "Toyota",description = "The brand get in the list of values")
    String brand;

    @RequiredField(fieldName = "Car model")
    @ValidModel(message = "INVALID_MODEL")
    @Schema(example = "Camry",description = "The model get in the list of values")
    String model;

    @RequiredField(fieldName = "Color of the car")
    @ValidColor(message = "INVALID_COLOR")
    @Schema(example = "Black",description = "The color get in the list of values")
    String color;

    @ValidNumberOfSeats(message = "INVALID_NUMBER_OF_SEAT")
    @Schema(example = "4",description = "Is only 4,5 or 7")
    int numberOfSeats;

    @ValidProductionYear(message = "INVALID_PRODUCTION_YEAR")
    @Schema(example = "2000",description = "from 1990 to 2030")
    int productionYear;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Mileage that the car has go")
    @Schema(example = "15000")
    float mileage;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @Schema(example = "7.5")
    float fuelConsumption;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Car's base price")
    @Schema(example = "1000000")
    long basePrice;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Deposit value")
    @Schema(example = "500000")
    long deposit;

    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    @Schema(example = "Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung,211 Trần Duy Hưng")
    String address;

    @Schema(example = "xe toyota camry")
    String description;

    @ValidAdditionalFunction(message = "INVALID_ADDITIONAL_FUNTION")
    @Schema(example = "Sun Roof, Bluetooth")
    String additionalFunction;

    @Schema(example = "No smoking")
    String termOfUse;

    @RequiredField(fieldName = "Transmission type")
    @Schema(example = "true")
    boolean isAutomatic;

    @RequiredField(fieldName = "Fuel type")
    @Schema(example = "true")
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
