package com.mp.karental.dto.response;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents the response payload for a booking.
 * <p>
 * This class encapsulates booking information that is returned to the client,
 * including car details.
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
public class BookingResponse {
    String bookingNumber;

    String carId;

    EBookingStatus status;

    String pickUpLocation;

    LocalDateTime pickUpTime;

    LocalDateTime dropOffTime;

    long totalPrice;

    long basePrice;

    long deposit;

    EPaymentType paymentType;

    // Driver Information
    String driverFullName;

    String driverPhoneNumber;

    String driverNationalId;

    LocalDate driverDob;

    String driverEmail;

    String driverDrivingLicenseUrl;

    // Driver Address
    String driverCityProvince;

    String driverDistrict;

    String driverWard;

    String driverHouseNumberStreet;
}
