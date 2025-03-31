package com.mp.karental.dto.request.car;

import com.mp.karental.validation.RequiredField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Request DTO for retrieving car details within a specified date range.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "request.car.CarDetailRequest",description = "Request object for fetching car details within a specified date range.")
public class CarDetailRequest {

    @RequiredField(message = "carId")
    @Schema(description = "Unique identifier of the car", example = "c123456")
    private String carId;

    @RequiredField(message = "Pick-up time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Pick-up date and time in ISO format", example = "2025-04-01T10:00:00")
    private LocalDateTime pickUpTime;

    @RequiredField(message = "Drop-off time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Drop-off date and time in ISO format", example = "2025-04-05T15:00:00")
    private LocalDateTime dropOffTime;
}

