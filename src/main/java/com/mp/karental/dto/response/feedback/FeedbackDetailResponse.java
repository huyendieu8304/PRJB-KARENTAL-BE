package com.mp.karental.dto.response.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning detailed feedback information in reports.
 * This class provides a more comprehensive feedback response,
 * including booking details, customer review, and car details.
 *
 * @author AnhHP9
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
public class FeedbackDetailResponse {

    String bookingId;
    int rating;
    String comment;
    LocalDateTime createdAt;
    String reviewerName;

    LocalDateTime pickUpTime;
    LocalDateTime dropOffTime;

    // Car name
    String brand;
    String model;

    // Car images
    String carImageFrontUrl;

}
