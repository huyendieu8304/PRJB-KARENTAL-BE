package com.mp.karental.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.validation.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Represents the request payload for add a new car.
 *
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
    @NotBlank(message = "REQUIRED_FIELD")
    //form license plate:(11-99)(A-Z)-(000-999).(00-99)
    @Pattern(regexp = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$", message = "INVALID_LICENSE")
    @UniqueLicensePlate(message = "NOT_UNIQUE_LICENSE")
    String licensePlate;

    @NotBlank(message = "REQUIRED_FIELD")
    @ValidBrand(message = "INVALID_BRAND")
    String brand;

    @NotBlank(message = "REQUIRED_FIELD")
    @ValidModel(message = "INVALID_MODEL")
    String model;

    @NotBlank(message = "REQUIRED_FIELD")
    @ValidColor(message = "INVALID_COLOR")
    String color;
    @ValidNumberOfSeats(message = "INVALID_NUMBER_OF_SEAT")
    int numberOfSeats;
    @ValidProductionYear(message = "INVALID_PRODUCTION_YEAR")
    int productionYear;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @NotNull(message = "REQUIRED_FIELD")
    float mileage;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    float fuelConsumption;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @NotNull(message = "REQUIRED_FIELD")
    int basePrice;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @NotNull(message = "REQUIRED_FIELD")
    int deposit;

    @NotBlank(message = "REQUIRED_FIELD")
    @ValidAddress(message = "INVALID_ADDRESS")
    String address;

    String description;

    @ValidAdditionalFunction(message = "INVALID_ADDITIONAL_FUNTION")
    String additionalFunction;

    String termOfUse;

    boolean isAutomatic;
    boolean isGasoline;

    //  MultipartFile**
    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @NotNull(message = "REQUIRED_FIELD")
    MultipartFile registrationPaper;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @NotNull(message = "REQUIRED_FIELD")
    MultipartFile certificateOfInspection;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @NotNull(message = "REQUIRED_FIELD")
    MultipartFile insurance;

    @NotNull(message = "REQUIRED_FIELD")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageFront;

    @NotNull(message = "REQUIRED_FIELD")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageBack;

    @NotNull(message = "REQUIRED_FIELD")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageLeft;

    @NotNull(message = "REQUIRED_FIELD")
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageRight;

}
