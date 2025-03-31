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
@Schema(description = "Response containing booking details.")
public class BookingResponse {

    @Schema(description = "Unique identifier for the booking", example = "BK-20240329-001")
    String bookingNumber;

    @Schema(description = "Unique identifier for the car", example = "CAR-12345")
    String carId;

    @Schema(description = "Current status of the booking", implementation = EBookingStatus.class)
    EBookingStatus status;

    @Schema(description = "Pickup location", example = "123 Main Street, New York, NY")
    String pickUpLocation;

    @Schema(description = "Pickup date and time", example = "2025-04-01T10:00:00")
    LocalDateTime pickUpTime;

    @Schema(description = "Drop-off date and time", example = "2025-04-05T18:00:00")
    LocalDateTime dropOffTime;

    @Schema(description = "Total price of the booking", example = "1500000")
    long totalPrice;

    @Schema(description = "Base price of the car rental", example = "1200000")
    long basePrice;

    @Schema(description = "Deposit amount required for the booking", example = "300000")
    long deposit;

    @Schema(description = "Payment method used", implementation = EPaymentType.class)
    EPaymentType paymentType;

    // Driver Information
    @Schema(description = "Full name of the driver", example = "Nguyen Van A")
    String driverFullName;

    @Schema(description = "Phone number of the driver", example = "+84901234567")
    String driverPhoneNumber;

    @Schema(description = "National ID of the driver", example = "123456789")
    String driverNationalId;

    @Schema(description = "Date of birth of the driver", example = "1990-05-15")
    LocalDate driverDob;

    @Schema(description = "Email of the driver", example = "nguyenvana@example.com")
    String driverEmail;

    @Schema(description = "URL to the driver's driving license image", example = "https://example.com/license.jpg")
    String driverDrivingLicenseUrl;

    // Driver Address
    @Schema(description = "City/Province where the driver resides", example = "Hanoi")
    String driverCityProvince;

    @Schema(description = "District where the driver resides", example = "Cau Giay")
    String driverDistrict;

    @Schema(description = "Ward where the driver resides", example = "Dich Vong")
    String driverWard;

    @Schema(description = "House number and street of the driver", example = "123 Hoang Quoc Viet")
    String driverHouseNumberStreet;

    @Schema(description = "Indicates whether the driver is the one renting the car", example = "true")
    boolean isDriver;
}
