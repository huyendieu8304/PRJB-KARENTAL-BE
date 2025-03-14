package com.mp.karental.dto.request.booking;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.ValidDocument;
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
public class EditBookingRequest {
    @RequiredField(fieldName = "car id")
    String carId;

    //driver
    //=============================================================
    @NotBlank(message = "Driver's full name is required")
    String driverFullName;

    @NotBlank(message = "Driver's phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Invalid phone number format")
    String driverPhoneNumber;

    @NotBlank(message = "Driver's national ID is required")
    @Pattern(regexp = "\\d{9,12}", message = "Invalid national ID format")
    String driverNationalId;

    @Past(message = "Driver's date of birth must be in the past")
    LocalDate driverDob;

    @NotBlank(message = "Driver's email is required")
    @Email(message = "Invalid email format")
    String driverEmail;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    MultipartFile driverDrivingLicense;


    @NotBlank(message = "City/Province is required")
    String driverCityProvince;

    @NotBlank(message = "District is required")
    String driverDistrict;

    @NotBlank(message = "Ward is required")
    String driverWard;

    @NotBlank(message = "House number and street are required")
    String driverHouseNumberStreet;

    boolean isDriver;
}
