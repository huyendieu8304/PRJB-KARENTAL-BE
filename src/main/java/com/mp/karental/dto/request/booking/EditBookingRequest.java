package com.mp.karental.dto.request.booking;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.ValidAge;
import com.mp.karental.validation.ValidDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Represents the request payload for booking.
 * <p>
 * This class encapsulates the necessary data required to edit a booking,
 * including booking information.
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
@Schema(name = "request.booking.EditBookingRequest", description = "DTO contain necessary information to make a car")
public class EditBookingRequest {
    //driver
    //=============================================================
    @Schema(example = "John Doe", description = "The full name of the driver")
    String driverFullName;
    @Schema(example = "0886980035", description = "The phone number of the driver")
    String driverPhoneNumber;
    @Schema(example = "A123456789", description = "The national ID of the driver")
    String driverNationalId;

    @ValidAge(min = 18)
    @Schema(example = "2004-11-08", description = "The date of birth of the driver (must be at least 18 years old)")
    LocalDate driverDob;

    @Email(message = "INVALID_EMAIL")
    @Schema(example = "john.doe@example.com", description = "The email address of the driver")
    String driverEmail;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    @Schema(type = "string", format = "binary", description = "Driver driving license file (.doc, .docx, .pdf, .jpeg, .jpg, .png)")
    MultipartFile driverDrivingLicense;

    @Schema(example = "Tỉnh Hà Giang", description = "The city or province of the driver")
    String driverCityProvince;
    @Schema(example = "Thành phố Hà Giang", description = "The district of the driver")
    String driverDistrict;
    @Schema(example = "Phường Quang Trung", description = "The ward of the driver")
    String driverWard;
    @Schema(example = "211 Trần Duy Hưng", description = "The house number and street of the driver")
    String driverHouseNumberStreet;

    // is the driver of the booking is different from the renter(true) or renter is driver of this booking (false)
    @Schema(example = "true", description = "Indicates if the driver is different from the renter (true) or the renter is also the driver (false)")
    boolean isDriver;
}
