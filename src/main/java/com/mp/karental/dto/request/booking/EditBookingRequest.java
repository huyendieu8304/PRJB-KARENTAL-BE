package com.mp.karental.dto.request.booking;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.ValidAge;
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
    //driver
    //=============================================================
    String driverFullName;

    String driverPhoneNumber;

    String driverNationalId;

    @ValidAge(min = 18)
    LocalDate driverDob;

    @Email(message = "Invalid email format")
    String driverEmail;

    @ValidDocument(message = "INVALID_DOCUMENT_FILE")
    MultipartFile driverDrivingLicense;


    String driverCityProvince;

    String driverDistrict;

    String driverWard;

    String driverHouseNumberStreet;

    // is the driver of the booking is different from the renter(true) or renter is driver of this booking (false)
    boolean isDriver;
}
