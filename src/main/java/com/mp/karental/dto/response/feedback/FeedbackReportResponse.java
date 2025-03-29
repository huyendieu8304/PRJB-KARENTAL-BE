package com.mp.karental.dto.response.feedback;

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
public class FeedbackReportResponse {

    List<FeedbackDetailResponse> feedbacks;

    // Information of page
    private int totalPages;
    private int pageSize;
    private long totalElements;

}
