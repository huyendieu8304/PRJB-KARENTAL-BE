package com.mp.karental.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mp.karental.entity.UserProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
public class AddCarRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    //form license plate:(11-99)(A-Z)-(000-999).(00-99)
    @Pattern(regexp = "^(1[1-9]|[2-9][0-9])[A-Z]-(\\d{3})\\.(\\d{2})$", message = "INVALID_LICENSE")
    String licensePlate;

    @NotBlank(message = "REQUIRED_FIELD")
    String brand;

    @NotBlank(message = "REQUIRED_FIELD")
    String model;

    @NotBlank(message = "REQUIRED_FIELD")
    String color;

    int numberOfSeats;

    int productionYear;

    float mileage;

    float fuelConsumption;

    int basePrice;

    int deposit;
    int reservationPrice;

    @NotBlank(message = "REQUIRED_FIELD")
    String address;

    String description;


    String additionalFunction;


    String termOfUse;

    @Column(nullable = false)
    boolean isAutomatic = true;
    @Column(nullable = false)
    boolean isGasoline = true;

    //  MultipartFile**
    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile registrationPaper;

    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile certificateOfInspection;

    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile insurance;

    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile carImageFront;

    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile carImageBack;

    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile carImageLeft;

    @NotBlank(message = "REQUIRED_FIELD")
    MultipartFile carImageRight;
}
