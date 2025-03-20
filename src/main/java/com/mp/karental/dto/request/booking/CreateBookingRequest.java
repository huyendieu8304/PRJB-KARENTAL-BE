package com.mp.karental.dto.request.booking;

import com.mp.karental.constant.EPaymentType;
import com.mp.karental.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
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
public class CreateBookingRequest {
    @RequiredField(fieldName = "car id")
    String carId;

    @CreationTimestamp
    LocalDateTime createdAt;

    @ValidAddress(message = "INVALID_ADDRESS")
    String pickUpLocation;

    LocalDateTime pickUpTime;

    LocalDateTime dropOffTime;

    @ValidPaymentType
    EPaymentType paymentType;

    //driver
    //=============================================================
    String driverFullName;

    @Pattern(regexp = "\\d{10}", message = "Invalid phone number format")
    String driverPhoneNumber;

    @Pattern(regexp = "\\d{9,12}", message = "Invalid national ID format")
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
