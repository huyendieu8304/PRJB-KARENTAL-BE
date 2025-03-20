package com.mp.karental.dto.response.feedback;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * DTO for returning feedback details in API responses.
 * This class is immutable and built using Lombok's @Value and @Builder annotations.
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
}
