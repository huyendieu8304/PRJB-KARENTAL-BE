package com.mp.karental.dto.response.booking;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "response.booking.BookingResponse", description = "DTO containing information about a booking response")
public class BookingResponse {
    @Schema(example = "BK202410200001", description = "The unique booking number")
    String bookingNumber;
    @Schema(example = "car1", description = "The ID of the booked car")
    String carId;
    @Schema(description = "The current status of the booking", example = "WAITING_CONFIRMED")
    EBookingStatus status;
    @Schema(example = "Tỉnh Hà Giang, Thành phố Hà Giang, Phường Quang Trung, 211 Trần Duy Hưng", description = "The pickup location for the car rental")
    String pickUpLocation;
    @Schema(example = "2004-11-08T09:00:00", description = "The pickup time for the rental car")
    LocalDateTime pickUpTime;
    @Schema(example = "2004-11-08T18:00:00", description = "The drop-off time for the rental car")
    LocalDateTime dropOffTime;
    @Schema(example = "50000", description = "The total price for the booking")
    long totalPrice;
    @Schema(example = "50000", description = "The base price for booking per day of this car")
    long basePrice;
    @Schema(example = "10000", description = "The deposit amount for the booking")
    long deposit;
    @Schema(description = "The payment type for the booking", example = "WALLET")
    EPaymentType paymentType;

    // Driver Information
    @Schema(example = "John Doe", description = "The full name of the driver")
    String driverFullName;
    @Schema(example = "0886980035", description = "The phone number of the driver")
    String driverPhoneNumber;
    @Schema(example = "A123456789", description = "The national ID of the driver")
    String driverNationalId;
    @Schema(example = "2004-11-08", description = "The date of birth of the driver (must be at least 18 years old)")
    LocalDate driverDob;
    @Schema(example = "john.doe@example.com", description = "The email address of the driver")
    String driverEmail;
    @Schema(description = "URL of the driver's driving license image", example = "booking/123456/driver-driving-license.jpg")
    String driverDrivingLicenseUrl;

    // Driver Address
    @Schema(example = "Tỉnh Hà Giang", description = "The city or province of the driver")
    String driverCityProvince;
    @Schema(example = "Thành phố Hà Giang", description = "The district of the driver")
    String driverDistrict;
    @Schema(example = "Phường Quang Trung", description = "The ward of the driver")
    String driverWard;
    @Schema(example = "211 Trần Duy Hưng", description = "The house number and street of the driver")
    String driverHouseNumberStreet;
    @Schema(example = "true", description = "Indicates if the driver is different from the renter (true) or the renter is also the driver (false)")
    boolean isDriver;
}
