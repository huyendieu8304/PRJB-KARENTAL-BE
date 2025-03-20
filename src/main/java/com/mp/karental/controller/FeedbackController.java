package com.mp.karental.controller;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
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
     * @return ApiResponse containing a list of feedback responses.
     */
    @GetMapping("/customer/view-ratings/{bookingId}")
    public ApiResponse<List<FeedbackResponse>> getFeedbackByBookingId(@PathVariable String bookingId) {
        return ApiResponse.<List<FeedbackResponse>>builder()
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
}
