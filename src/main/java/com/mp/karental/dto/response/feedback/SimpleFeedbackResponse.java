package com.mp.karental.dto.response.feedback;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for simplified feedback response (only includes rating, comment, createdAt, and reviewerName).
 *
 *  * @author AnhHP9
 *  * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SimpleFeedbackResponse {
    int rating;
    String comment;
    LocalDateTime createdAt;
    String reviewerName;
}
