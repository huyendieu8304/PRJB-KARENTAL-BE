package com.mp.karental.dto.response.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "response.car.SimpleFeedbackResponse",description = "Simplified feedback response containing only rating, comment, createdAt, and reviewerName.")
public class SimpleFeedbackResponse {

    @Schema(description = "Customer rating for the car (1-5 stars)", example = "5")
    int rating;

    @Schema(description = "Customer's review comment", example = "Great experience! The car was clean and comfortable.")
    String comment;

    @Schema(description = "Timestamp when the feedback was created", example = "2024-03-29T10:15:30")
    LocalDateTime createdAt;

    @Schema(description = "Name of the reviewer", example = "John Doe")
    String reviewerName;
}
