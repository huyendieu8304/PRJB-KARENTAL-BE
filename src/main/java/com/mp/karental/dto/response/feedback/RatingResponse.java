package com.mp.karental.dto.response.feedback;

import java.util.Map;

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
public class RatingResponse {
    // Total feedback follow by rating (1-5 start)
    private Map<Integer, Long> ratingCounts;

    private Double averageRatingByOwner;
}
