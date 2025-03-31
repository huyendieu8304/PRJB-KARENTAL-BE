package com.mp.karental.dto.request.car;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.ValidAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Request DTO for searching available cars within a specified date range and filters.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for searching available cars within a specified date range and location filter.")
public class SearchCarRequest {

    @RequiredField(message = "Pick-up time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Pick-up date and time in ISO format", example = "2025-04-01T10:00:00")
    private LocalDateTime pickUpTime;

    @RequiredField(message = "Drop-off time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Drop-off date and time in ISO format", example = "2025-04-05T15:00:00")
    private LocalDateTime dropOffTime;

    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    @Schema(description = "Pick-up location of the car", example = "123 Main Street, New York, NY")
    String address;
}