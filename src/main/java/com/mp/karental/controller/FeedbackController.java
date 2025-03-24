package com.mp.karental.controller;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.feedback.FeedbackReportResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.dto.response.feedback.RatingResponse;
import com.mp.karental.service.FeedbackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling feedback-related operations.
 * Provides endpoints for customers to submit and view their feedback,
 * and for retrieving all feedback related to a specific car.
 *
 * @author AnhHP9
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FeedbackController {
    FeedbackService feedbackService;

    /**
     * API for customers to submit feedback (give rating) for a booking.
     *
     * @param request Feedback request containing booking ID, rating, and comment.
     * @return ApiResponse containing the submitted feedback details.
     */
    @PostMapping("/customer/give-rating")
    public ApiResponse<FeedbackResponse> giveRating(@RequestBody FeedbackRequest request) {
        return ApiResponse.<FeedbackResponse>builder()
                .data(feedbackService.addFeedback(request))
                .message("Feedback submitted successfully.")
                .build();
    }

    /**
     * API to retrieve feedback given by a customer for a specific booking.
     *
     * @param bookingId The ID of the booking for which feedback is requested.
     * @return ApiResponse containing the feedback response.
     */
    @GetMapping("/customer/view-ratings/{bookingId}")
    public ApiResponse<FeedbackResponse> getFeedbackByBookingId(@PathVariable String bookingId) {
        return ApiResponse.<FeedbackResponse>builder()
                .data(feedbackService.getFeedbackByBookingId(bookingId))
                .message("Feedback retrieved successfully.")
                .build();
    }


    /**
     * API to retrieve all feedback associated with a specific car.
     *
     * @param carId The ID of the car for which feedback is requested.
     * @return ApiResponse containing a list of feedback responses related to the car.
     */
    @GetMapping("/car/{carId}")
    public ApiResponse<List<FeedbackResponse>> getFeedbackByCarId(@PathVariable String carId) {
        return ApiResponse.<List<FeedbackResponse>>builder()
                .data(feedbackService.getFeedbackByCarId(carId))
                .message("Car feedback retrieved successfully.")
                .build();
    }

    /**
     * API endpoint for car owners to view their feedback reports.
     *
     * This endpoint allows car owners to retrieve a paginated list of feedback
     * left by customers for their cars. Owners can filter feedback based on
     * rating and sort them in descending order of creation date.
     *
     * @param ratingFilter (optional) The rating value to filter feedback (e.g., 5 for 5-star feedback).
     *                     Default is 0, meaning no filtering by rating.
     * @param page         (optional) The page number for pagination. Default is 0 (first page).
     * @param size         (optional) The number of feedback entries per page. Default is 10.
     * @return ApiResponse containing the feedback report, including the list of feedback
     *         details and the average rating for the owner's cars.
     */
    @GetMapping("/car-owner/my-feedbacks")
    public ApiResponse<FeedbackReportResponse> getOwnerFeedbackReport(
            @RequestParam(defaultValue = "0") int ratingFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<FeedbackReportResponse>builder()
                .data(feedbackService.getOwnerFeedbackReport(ratingFilter, page, size))
                .message("Feedback report retrieved successfully.")
                .build();
    }

    /**
     * API endpoint for car owners to retrieve their average rating and rating distribution.
     * <p>
     * This endpoint allows car owners to view their overall rating statistics, including:
     * - The average rating of all their cars.
     * - The number of feedback entries for each rating level (1-5 stars).
     * </p>
     *
     * @return ApiResponse containing the rating statistics.
     */
    @GetMapping("/car-owner/rating")
    public ApiResponse<RatingResponse> getRatingFeedbackReport() {
        return ApiResponse.<RatingResponse>builder()
                .data(feedbackService.getAverageRatingAndCountByCarOwner())
                .message("Car rating retrieved successfully.")
                .build();
    }

    /**
     * API endpoint for customer to view their feedback reports.
     *
     * This endpoint allows customers to retrieve a paginated list of feedback
     * left by them. Customers can filter feedback based on
     * rating and sort them in descending order of creation date.
     *
     * @param ratingFilter (optional) The rating value to filter feedback (e.g., 5 for 5-star feedback).
     *                     Default is 0, meaning no filtering by rating.
     * @param page         (optional) The page number for pagination. Default is 0 (first page).
     * @param size         (optional) The number of feedback entries per page. Default is 10.
     * @return ApiResponse containing the feedback report, including the list of feedback
     *         details and the average rating for the cars which is feedbacked by them.
     */
    @GetMapping("/customer/view-feedbacks")
    public ApiResponse<FeedbackReportResponse> getCustomerFeedbackReport(
            @RequestParam(defaultValue = "0") int ratingFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<FeedbackReportResponse>builder()
                .data(feedbackService.getCustomerFeedbackReport(ratingFilter, page, size))
                .message("Feedback report retrieved successfully.")
                .build();
    }


}
