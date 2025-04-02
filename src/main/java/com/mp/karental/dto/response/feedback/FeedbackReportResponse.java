package com.mp.karental.dto.response.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for returning feedback report of a car owner.
 * This report provides an overview of all feedback related to a car owner's vehicles,
 *
 * @author AnhHP9
 * @version 1.0
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Schema(name = "response.car.FeedbackReportResponse",description = "Response object containing a feedback report for a car owner, including a list of feedback and pagination details.")
public class FeedbackReportResponse {

    @Schema(
            description = "List of feedback details",
            example = "[{ \"bookingId\": \"12345\", \"rating\": 5, \"comment\": \"Great car!\", \"reviewerName\": \"John Doe\" }]"
    )
    List<FeedbackDetailResponse> feedbacks;

    // Pagination information
    @Schema(description = "Total number of pages available", example = "10")
    private int totalPages;

    @Schema(description = "Number of feedback items per page", example = "20")
    private int pageSize;

    @Schema(description = "Total number of feedback entries", example = "200")
    private long totalElements;
}
