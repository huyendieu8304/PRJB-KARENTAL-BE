package com.mp.karental.dto.response.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning feedback details in API responses.
 * This class represents the feedback left by a customer after completing a booking.
 * It includes rating, comment, and reviewer details.
 *
 * @author AnhHP9
 * @version 1.0
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Schema(description = "Response object containing feedback details for a booking.")
public class FeedbackResponse {

    @Schema(description = "Unique identifier for the booking associated with the feedback", example = "BK123456")
    String bookingId;

    @Schema(description = "Rating given by the customer, ranging from 1 to 5", example = "5")
    int rating;

    @Schema(description = "Optional comment provided by the customer", example = "Great experience! The car was in excellent condition.")
    String comment;

    @Schema(description = "Timestamp when the feedback was created", example = "2025-03-28T14:30:00")
    LocalDateTime createdAt;

    @Schema(description = "Full name of the reviewer who left the feedback", example = "John Doe")
    String reviewerName;
}
