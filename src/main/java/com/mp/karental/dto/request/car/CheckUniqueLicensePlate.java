package com.mp.karental.dto.request.car;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.UniqueLicensePlate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "request.car.CheckUniqueLicensePlate", description = "DTO contain necessary information to check unique license plate")
public class CheckUniqueLicensePlate {
    @RequiredField(fieldName = "License plate")
    @Pattern(regexp = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$", message = "INVALID_LICENSE")
    @UniqueLicensePlate(message = "NOT_UNIQUE_LICENSE")
    @Schema(example = "28F-125.13", pattern = "^(1[1-9]|[2-9][0-9])[A-Z]-\\d{3}\\.\\d{2}$", description = "The license plate number following the format (11-99)(A-Z)-(000-999).(00-99)")
    String licensePlate;
}
