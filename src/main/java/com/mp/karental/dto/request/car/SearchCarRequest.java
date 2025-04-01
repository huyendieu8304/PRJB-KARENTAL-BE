package com.mp.karental.dto.request.car;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.ValidAddress;
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
public class SearchCarRequest {

    @RequiredField(message = "Pick-up time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime pickUpTime;

    @RequiredField(message = "Drop-off time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dropOffTime;

    @RequiredField(fieldName = "Address")
    @ValidAddress(message = "INVALID_ADDRESS")
    String address;
}
