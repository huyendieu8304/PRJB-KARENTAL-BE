package com.mp.karental.dto.response.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for returning feedback report of a car owner.
 * This report provides an overview of all feedback related to a car owner's vehicles,
 * including the average rating and a list of detailed feedbacks.
 *
 * @author AnhHP9
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
public class FeedbackReportResponse {

    double averageRating;

    List<FeedbackDetailResponse> feedbacks;

}
