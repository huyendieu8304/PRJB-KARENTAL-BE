package com.mp.karental.dto.request.car;

import com.mp.karental.validation.RequiredField;
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
public class CarDetailRequest {
    @RequiredField(message = "carId")
    private String carId;

    @RequiredField(message = "Pick-up time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime pickUpTime;

    @RequiredField(message = "Drop-off time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dropOffTime;
}

