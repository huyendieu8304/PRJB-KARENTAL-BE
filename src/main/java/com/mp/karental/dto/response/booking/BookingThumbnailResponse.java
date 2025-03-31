package com.mp.karental.dto.response.booking;

import com.mp.karental.constant.EBookingStatus;
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
@Schema(description = "Compact response for booking details, including car information and pricing.")
public class BookingThumbnailResponse {

    @Schema(description = "Unique booking number", example = "BK202403291234")
    String bookingNumber;

    @Schema(description = "Current booking status", example = "CONFIRMED")
    EBookingStatus status;

    @Schema(description = "Pick-up time for the booking", example = "2024-04-01T10:00:00")
    LocalDateTime pickUpTime;

    @Schema(description = "Drop-off time for the booking", example = "2024-04-05T15:00:00")
    LocalDateTime dropOffTime;

    @Schema(description = "Number of rental days calculated from pick-up and drop-off time", example = "4")
    int numberOfDay;

    @Schema(description = "Base price per day of rental", example = "500000")
    long basePrice;

    @Schema(description = "Total price calculated as basePrice * numberOfDay", example = "2000000")
    long totalPrice;

    @Schema(description = "Deposit required for the booking", example = "1000000")
    long deposit;

    // Car details
    @Schema(description = "Car brand", example = "Toyota")
    String brand;

    @Schema(description = "Car model", example = "Camry")
    String model;

    @Schema(description = "Production year of the car", example = "2022")
    int productionYear;

    // Car images
    @Schema(description = "Front view image URL of the car", example = "https://example.com/car_front.jpg")
    String carImageFrontUrl;

    @Schema(description = "Back view image URL of the car", example = "https://example.com/car_back.jpg")
    String carImageBackUrl;

    @Schema(description = "Left side image URL of the car", example = "https://example.com/car_left.jpg")
    String carImageLeftUrl;

    @Schema(description = "Right side image URL of the car", example = "https://example.com/car_right.jpg")
    String carImageRightUrl;
}