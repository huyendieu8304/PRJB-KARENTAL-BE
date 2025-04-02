package com.mp.karental.dto.response.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning detailed feedback information in reports.
 * This class provides a more comprehensive feedback response,
 * including booking details, customer review, and car details.
 *
 * @author AnhHP9
 * @version 1.0
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Schema(name = "response.car.FeedbackDetailResponse",description = "Detailed feedback response including booking details, customer review, and car details.")
public class FeedbackDetailResponse {

    @Schema(description = "Unique identifier of the booking", example = "b12345")
    private String bookingId;

    @Schema(description = "Customer rating for the car (1-5 stars)", example = "5")
    private int rating;

    @Schema(description = "Customer's review comment", example = "The car was in excellent condition and very clean.")
    private String comment;

    @Schema(description = "Timestamp when the feedback was created", example = "2024-03-29T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Name of the reviewer", example = "John Doe")
    private String reviewerName;

    @Schema(description = "Pick-up time of the booking", example = "2024-03-25T08:00:00")
    private LocalDateTime pickUpTime;

    @Schema(description = "Drop-off time of the booking", example = "2024-03-28T18:00:00")
    private LocalDateTime dropOffTime;

    // Car details
    @Schema(description = "Car brand", example = "Toyota")
    private String brand;

    @Schema(description = "Car model", example = "Camry")
    private String model;

    // Car images
    @Schema(description = "URL of the car's front image", example = "https://example.com/images/car_front.jpg")
    private String carImageFrontUrl;
}