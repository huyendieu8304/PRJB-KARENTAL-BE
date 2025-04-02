package com.mp.karental.dto.request.car;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents the request payload for update a new car.
 * <p>
 * This class encapsulates the necessary data required to edit a car,
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
@Schema(name = "request.car.EditCarRequest", description = "DTO contain necessary information to edit a car")
public class EditCarRequest {
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Mileage that the car has go")
    @Schema(example = "15000", description = "The mileage of the car in kilometers")
    float mileage;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @Schema(example = "7.5", description = "The fuel consumption of the car in liters per 100 kilometers")
    float fuelConsumption;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Car's base price")
    @Schema(example = "1000000", description = "The base rental price of the car per day")
    long basePrice;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Deposit value")
    @Schema(example = "500000", description = "The deposit required for renting the car")
    long deposit;

    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    @Schema(example = "Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211 Trần Duy Hưng", description = "The address where the car is available for rent")
    String address;

    @Schema(example = "xe toyota camry", description = "A brief description of the car")
    String description;

    @ValidAdditionalFunction(message = "INVALID_ADDITIONAL_FUNTION")
    @Schema(example = "Sun Roof, Bluetooth", description = "Additional functions or features available in the car")
    String additionalFunction;

    @Schema(example = "No smoking", description = "Terms of use or rental restrictions for the car")
    String termOfUse;

    @Schema(example = "VERIFIED", description = "The status of this car")
    ECarStatus status;
    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @Schema(type = "string", format = "binary", description = "Registration paper file (.doc, .docx, .pdf, .jpeg, .jpg, .png)")
    MultipartFile registrationPaper;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @Schema(type = "string", format = "binary", description = "Certificate of inspection file (.doc, .docx, .pdf, .jpeg, .jpg, .png)")
    MultipartFile certificateOfInspection;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @Schema(type = "string", format = "binary", description = "Insurance file (.doc, .docx, .pdf, .jpeg, .jpg, .png)")
    MultipartFile insurance;
    //  MultipartFile**
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    @Schema(type = "string", format = "binary", description = "Car's front side image (.jpg, .jpeg, .png, .gif)")
    MultipartFile carImageFront;

    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    @Schema(type = "string", format = "binary", description = "Car's back side image (.jpg, .jpeg, .png, .gif)")
    MultipartFile carImageBack;

    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    @Schema(type = "string", format = "binary", description = "Car's left side image (.jpg, .jpeg, .png, .gif)")
    MultipartFile carImageLeft;

    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    @Schema(type = "string", format = "binary", description = "Car's right side image (.jpg, .jpeg, .png, .gif)")
    MultipartFile carImageRight;
}
