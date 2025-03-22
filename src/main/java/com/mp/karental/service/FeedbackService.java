package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.feedback.FeedbackDetailResponse;
import com.mp.karental.dto.response.feedback.FeedbackReportResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Feedback;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.FeedbackMapper;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.FeedbackRepository;
import com.mp.karental.security.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling feedback operations.
 * <p>
 * This service provides methods for customers to submit feedback,
 * retrieve feedback for bookings or cars, and generate reports for car owners.
 * </p>
 * <p>
 * Author: AnhPH9
 * Version: 1.0
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService {
    FeedbackRepository feedbackRepository;
    BookingRepository bookingRepository;
    FeedbackMapper feedbackMapper;
    CarRepository carRepository;

    /**
     * Adds feedback (rating + comment) for a completed booking.
     * <p>
     * Validates that the booking exists, is completed, and has not already received feedback.
     * Also ensures that feedback is submitted within 30 days after drop-off.
     * </p>
     *
     * @param request Feedback data from the customer.
     * @return The saved feedback response.
     * @throws AppException if the booking does not exist, is not completed, or feedback already exists.
     */
    public FeedbackResponse addFeedback(FeedbackRequest request) {
        // Find booking by booking ID
        Booking booking = bookingRepository.findBookingByBookingNumber(request.getBookingId());

        // Ensure the booking is completed before allowing feedback submission
        if (booking.getStatus() != EBookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_NOT_COMPLETED);
        }

        // Check if feedback already exists for this booking
        if (feedbackRepository.existsById(request.getBookingId())) {
            throw new AppException(ErrorCode.FEEDBACK_ALREADY_EXISTS);
        }

        // Check if feedback is within 30 days after drop-off
        if (booking.getDropOffTime().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.FEEDBACK_TIME_EXPIRED);
        }

        // Validate feedback length (max 250 characters)
        if (request.getComment() != null && request.getComment().length() > 250) {
            throw new AppException(ErrorCode.FEEDBACK_TOO_LONG);
        }

        // Convert request DTO to Feedback entity
        Feedback feedback = feedbackMapper.toFeedback(request);
        feedback.setBooking(booking);

        // Save feedback to the database
        feedback = feedbackRepository.save(feedback);
        FeedbackResponse response = feedbackMapper.toFeedbackResponse(feedback);
        response.setCreatedAt(feedback.getCreateAt());

        // Return the response after saving
        return response;
    }

    /**
     * Retrieves all feedback for a specific booking.
     * <p>
     * If no feedback is found, an empty list is returned.
     * </p>
     *
     * @param bookingId The booking ID.
     * @return A list of feedback or an empty list if no feedback is found.
     */
    public List<FeedbackResponse> getFeedbackByBookingId(String bookingId) {
        // Fetch feedback list based on booking ID
        List<Feedback> feedbackList = feedbackRepository.findByBookingNumber(bookingId);

        // Log a warning and return an empty list if no feedback is found
        if (feedbackList.isEmpty()) {
            log.warn("No feedback found for bookingId {}", bookingId);
            return Collections.emptyList();
        }

        // Convert feedback entities to response DTOs
        return feedbackList.stream()
                .map(feedbackMapper::toFeedbackResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all feedback for a specific car.
     * <p>
     * Converts feedback entity data into response DTOs.
     * </p>
     *
     * @param carId The car ID.
     * @return A list of feedback related to the car.
     */
    public List<FeedbackResponse> getFeedbackByCarId(String carId) {
        return feedbackRepository.findByCarId(carId)
                .stream()
                .map(feedbackMapper::toFeedbackResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the feedback report for a car owner.
     * <p>
     * Fetches the list of feedback for all cars owned by the authenticated user.
     * If a rating filter is applied, only feedback with the specified rating is retrieved.
     * </p>
     *
     * @param ratingFilter The rating filter (1-5 stars or 0 for all).
     * @param page         The page number (default 0).
     * @param size         The page size (default 10).
     * @return FeedbackReportResponse containing the filtered feedback.
     */
    @Transactional(readOnly = true)
    public FeedbackReportResponse getFilteredFeedbackReport(int ratingFilter, int page, int size) {
        log.info("Fetching feedback report for ratingFilter={}, page={}, size={}", ratingFilter, page, size);

        // Get the current owner's ID
        String ownerId = SecurityUtil.getCurrentAccountId();

        // Retrieve the list of car IDs owned by the user
        List<String> carIds = carRepository.findCarIdsByOwnerId(ownerId);
        if (carIds.isEmpty()) {
            log.warn("No cars found for ownerId={}", ownerId);
            return FeedbackReportResponse.builder()
                    .averageRating(0.0)
                    .feedbacks(Collections.emptyList())  // Return an empty list if no feedback exists
                    .build();
        }

        // Set up pagination and sorting
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));

        // Fetch feedback based on rating filter
        Page<Feedback> feedbackPage = (ratingFilter > 0)
                ? feedbackRepository.findByCarIdsAndRating(carIds, ratingFilter, pageRequest)
                : feedbackRepository.findByCarIds(carIds, pageRequest);

        List<Feedback> feedbackList = feedbackPage.getContent();
        if (feedbackList.isEmpty()) {
            log.warn("No feedback found for the given filters");
        }

        // Calculate the average rating
        double averageRating = calculateAverageRating(feedbackList);

        // Convert to response DTOs
        List<FeedbackDetailResponse> feedbackDetails = feedbackMapper.toFeedbackDetailResponseList(feedbackList);

        return FeedbackReportResponse.builder()
                .averageRating(averageRating)
                .feedbacks(feedbackDetails)  // Return the list of feedbacks
                .build();
    }

    /**
     * Calculates the average rating from a list of feedback.
     * <p>
     * If no feedback exists, returns 0.0.
     * </p>
     *
     * @param feedbackList The list of feedback.
     * @return The average rating rounded to 2 decimal places.
     */
    private double calculateAverageRating(List<Feedback> feedbackList) {
        if (feedbackList.isEmpty()) return 0.0;

        double sum = feedbackList.stream().mapToInt(Feedback::getRating).sum();
        double avg = sum / feedbackList.size();

        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
