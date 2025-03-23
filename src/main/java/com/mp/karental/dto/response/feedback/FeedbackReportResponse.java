package com.mp.karental.dto.response.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

    // Thêm thông tin phân trang
    private int totalPages;
    private int pageSize;
    private long totalElements;

    // Tổng số feedback theo từng rating (1-5 sao)
    private Map<Integer, Long> ratingCounts;

    private Double averageRatingByOwner;

    private Map<String, Double> averageRatingByCar; }
