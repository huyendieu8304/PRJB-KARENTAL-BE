package com.mp.karental.controller;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.feedback.FeedbackReportResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.dto.response.feedback.RatingResponse;
import com.mp.karental.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Feedback", description = "API for managing feedback")
public class FeedbackController {
    FeedbackService feedbackService;

    /**
     * API for customers to submit feedback (give rating) for a booking.
     *
     * @param request Feedback request containing booking ID, rating, and comment.
     * @return ApiResponse containing the submitted feedback details.
     */
    @Operation(
            summary = "Give rating",
            description = "Customer can give rating to the car.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = FeedbackResponse.class)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad request
                                    |code  | message |
                                    |------|-------------|
                                    | 3033 | This booking is not COMPLETED.|
                                    | 3035 | This booking is already exists.|
                                    | 3031 | Feedback is only allowed within 30 days after drop-off.|
                                    | 3032 | Feedback content must not exceed 2000 characters.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
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
    @Operation(
            summary = "View rating",
            description = "Customer can view their rating to the car.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = FeedbackResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/customer/view-ratings/{bookingId}")
    public ApiResponse<FeedbackResponse> getFeedbackByBookingId(@PathVariable @Parameter(description = "The booking id to be vew rating.", example = "BK001") String bookingId) {
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
    @Operation(
            summary = "View all feedback of car",
            description = "Anyone can view all feedback of car.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = FeedbackResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/car/{carId}")
    public ApiResponse<List<FeedbackResponse>> getFeedbackByCarId(@PathVariable @Parameter(description = "The car id to be vew feedback.", example = "car1") String carId) {
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
    @Operation(
            summary = "View feedback reports",
            description = "Car owner can view their feedback reports.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = FeedbackReportResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
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
    @Operation(
            summary = "View rating",
            description = "Car owner can view their average rating and rating distribution.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = RatingResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
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
    @Operation(
            summary = "View feedback reports",
            description = "Customer can view their feedback reports.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = FeedbackReportResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
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
