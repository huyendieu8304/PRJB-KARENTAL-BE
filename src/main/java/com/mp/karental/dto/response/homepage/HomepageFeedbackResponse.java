package com.mp.karental.dto.response.homepage;

import com.mp.karental.dto.response.feedback.SimpleFeedbackResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * DTO for Homepage response.
 * Includes latest 4 five-star feedbacks.
 *
 * @author AnhHP9
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(description = "Response object containing the latest 4 five-star feedbacks.")
public class HomepageFeedbackResponse {

    @Schema(description = "List of the latest five-star feedbacks.",
            example = "[{\"rating\": 5, \"comment\": \"Great service!\", \"createdAt\": \"2024-03-29T12:00:00\", \"reviewerName\": \"John Doe\"}]")
    List<SimpleFeedbackResponse> latestFiveStarFeedbacks;
}