package com.mp.karental.dto.response.feedback;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO for returning rating feedback report of a car owner.
 * This report provides Total feedback follow by rating (1-5 start)
 * and the average rating by car owner.
 *
 * @author AnhHP9
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response object containing rating statistics for a car owner.")
public class RatingResponse {

    @Schema(
            description = "Total feedback count grouped by rating (1-5 stars)",
            example = "{\"1\": 10, \"2\": 5, \"3\": 8, \"4\": 20, \"5\": 50}"
    )
    private Map<Integer, Long> ratingCounts;

    @Schema(
            description = "The average rating received by the car owner, calculated from all feedback",
            example = "4.5"
    )
    private Double averageRatingByOwner;
}