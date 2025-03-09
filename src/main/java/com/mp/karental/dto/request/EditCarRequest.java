package com.mp.karental.dto.request;

import com.mp.karental.validation.*;
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
public class EditCarRequest {
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Mileage that the car has go")
    float mileage;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    float fuelConsumption;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Car's base price")
    int basePrice;
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @RequiredField(fieldName = "Deposit value")
    int deposit;

    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    String address;

    String description;

    @ValidAdditionalFunction(message = "INVALID_ADDITIONAL_FUNTION")
    String additionalFunction;

    String termOfUse;

    @ValidStatusEdit(message = "INVALID_STATUS_EDIT")
    String status;

    //  MultipartFile**
    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageFront;

    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageBack;

    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageLeft;

    @ValidImageCar(message = "INVALID_CAR_IMAGE_FILE")
    MultipartFile carImageRight;
}
