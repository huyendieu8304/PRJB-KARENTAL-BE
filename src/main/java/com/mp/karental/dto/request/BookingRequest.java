package com.mp.karental.dto.request;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.validation.ValidAddressComponent;
import com.mp.karental.validation.ValidBookingTime;
import com.mp.karental.validation.ValidDocument;
import com.mp.karental.validation.ValidPaymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents the request payload for booking.
 * <p>
 * This class encapsulates the necessary data required to create a booking,
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
@ValidBookingTime(message = "INVALID_BOOKING_TIME")
@ValidAddressComponent(message = "INVALID_ADDRESS_COMPONENT")
public class BookingRequest {
    String pickUpLocation;

    LocalDateTime pickUpTime;

    LocalDateTime dropOffTime;

    @ValidPaymentType
    EPaymentType paymentType;

    //driver
    //=============================================================
    @NotBlank(message = "Driver's full name is required")
    String driverFullName;

    @NotBlank(message = "Driver's phone number is required")
    @Pattern(regexp = "\\d{10,15}", message = "Invalid phone number format")
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
}
