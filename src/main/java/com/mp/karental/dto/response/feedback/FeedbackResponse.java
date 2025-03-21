package com.mp.karental.dto.response.feedback;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning feedback details in API responses.
 * This class represents the feedback left by a customer after completing a booking.
 * It includes rating, comment, and reviewer details.
 *
 * @author AnhHP9
 * @version 1.0
 */

@Builder
@Data
public class FeedbackResponse {
    String bookingId;
    int rating;
    String comment;
    LocalDateTime createdAt;
    String reviewerName;

}
