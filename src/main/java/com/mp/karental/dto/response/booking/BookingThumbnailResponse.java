package com.mp.karental.dto.response.booking;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Represents the response payload for a booking.
 * <p>
 * This class encapsulates booking information that is returned to the client,
 * including car details.
 * </p>
 * @author AnhHP9
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "response.booking.BookingThumbnailResponse", description = "Summary response of a booking with car and payment details.")
public class BookingThumbnailResponse {
    @Schema(example = "BKG123456", description = "Unique booking number.")
    String bookingNumber;
    @Schema(description = "Current status of the booking.", example = "CONFIRMED")
    EBookingStatus status;
    @Schema(example = "2024-10-21T10:00:00", description = "Pickup time for the booking.")
    LocalDateTime pickUpTime;
    @Schema(example = "2024-10-23T10:00:00", description = "Drop-off time for the booking.")
    LocalDateTime dropOffTime;

    //(dropOffTime - pickupTime)%24
    @Schema(example = "2", description = "Number of rental days calculated from dropOffTime - pickUpTime.")
    int numberOfDay;
    @Schema(example = "50000", description = "Base price per day for the car rental.")
    long basePrice;

    //basePrice * numberOfDay
    @Schema(example = "100000", description = "Total rental price calculated as basePrice * numberOfDay.")
    long totalPrice;
    @Schema(example = "20000", description = "Deposit amount required for the booking.")
    long deposit;

    //join car table
    @Schema(example = "Toyota", description = "Brand of the car.")
    String brand;
    @Schema(example = "Camry", description = "Model of the car.")
    String model;

    //sort desc
    @Schema(example = "2022", description = "Production year of the car (sorted in descending order).")
    int productionYear;

    //car image
    @Schema(description = "URL of the car's front side image", example = "car/12345/car1/images/car-front.jpg")
    String carImageFrontUrl;

    @Schema(description = "URL of the car's back side image", example = "car/12345/car1/images/car-back.jpg")
    String carImageBackUrl;

    @Schema(description = "URL of the car's left side image", example = "car/12345/car1/images/car-left.jpg")
    String carImageLeftUrl;

    @Schema(description = "URL of the car's right side image", example = "car/12345/car1/images/car-right.jpg")
    String carImageRightUrl;
    @Schema(example = "0886980035", description = "Phone number of the customer who booked the car.")
    String customerPhoneNumber;
    @Schema(example = "customer@example.com", description = "Email address of the customer.")
    String customerEmail;
    @Schema(description = "Payment type selected for the booking.", example = "WALLET")
    EPaymentType paymentType;

}
