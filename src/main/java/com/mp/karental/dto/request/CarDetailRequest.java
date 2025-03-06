package com.mp.karental.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Request DTO for retrieving car details within a specified date range.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarDetailRequest {
    @NotNull(message = "Car ID is required")
    private String carId;

    @NotNull(message = "Pick-up date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime pickUpTime;

    @NotNull(message = "Drop-off date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dropOffTime;
}

