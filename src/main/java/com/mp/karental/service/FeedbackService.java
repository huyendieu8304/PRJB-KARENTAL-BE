package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.feedback.FeedbackDetailResponse;
import com.mp.karental.dto.response.feedback.FeedbackReportResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.dto.response.feedback.RatingResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    FileService fileService;

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
        if (request.getComment() != null && request.getComment().length() > 2000) {
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
     * Retrieves feedback for a specific booking.
     * If no feedback is found, returns null.
     *
     * @param bookingId The booking ID.
     * @return The feedback response or null if not found.
     */
    public FeedbackResponse getFeedbackByBookingId(String bookingId) {
        // Fetch feedback based on booking ID
        Feedback feedback = feedbackRepository.findByBookingNumber(bookingId)
                .orElse(null);

        // Log a warning if no feedback is found
        if (feedback == null) {
            log.warn("No feedback found for bookingId {}", bookingId);
            return null;
        }

        // Convert feedback entity to response DTO
        return feedbackMapper.toFeedbackResponse(feedback);
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
            return buildFeedbackReportResponse(Collections.emptyList(), 0, size, 0);

        }

        // Get number of feedback by rating
        List<Object[]> ratingCountData = feedbackRepository.countFeedbackByRating(carIds);
        Map<Integer, Long> ratingCounts = new java.util.HashMap<>(ratingCountData.isEmpty()
                ? Collections.emptyMap()
                : ratingCountData.stream().collect(Collectors.toMap(
                data -> ((Number) data[0]).intValue(),
                data -> ((Number) data[1]).longValue()
        )));

        // Ensure rating from 1 to 5 always have ratingCounts, if not equal 0
        for (int i = 1; i <= 5; i++) {
            ratingCounts.putIfAbsent(i, 0L);
        }

        // Count total feedback based on ratingFilter
        long totalElements = (ratingFilter > 0) ? ratingCounts.getOrDefault(ratingFilter, 0L)
                : ratingCounts.values().stream().mapToLong(Long::longValue).sum();

        // Calculate total pages correctly based on filtered totalElements
        int totalPages = (size > 0 && totalElements > 0) ? (int) Math.ceil((double) totalElements / size) : 0;

        // Set up pagination and sorting
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));

        // Fetch feedback based on the rating filter
        Page<Feedback> feedbackPage = (ratingFilter > 0)
                ? feedbackRepository.findByCarIdsAndRating(carIds, ratingFilter, pageRequest)
                : feedbackRepository.findByCarIds(carIds, pageRequest);

        List<Feedback> feedbackList = feedbackPage.getContent();

        // Return immediately if no feedback is found
        if (feedbackList.isEmpty()) {
            log.warn("No feedback found for the given filters");
            return buildFeedbackReportResponse(
                    Collections.emptyList(), totalPages, size, totalElements);
        }

        // Convert feedback entities to DTOs
        List<FeedbackDetailResponse> feedbackDetails = feedbackMapper.toFeedbackDetailResponseList(feedbackList);

        // Convert URI to URL
        for (FeedbackDetailResponse feedback : feedbackDetails) {
            feedback.setCarImageFrontUrl(fileService.getFileUrl(feedback.getCarImageFrontUrl()));
        }

        return buildFeedbackReportResponse(feedbackDetails, totalPages, size, totalElements);

    }

    /**
     * Helper method to build a FeedbackReportResponse object.
     * <p>
     * This method constructs a FeedbackReportResponse containing a paginated list of feedbacks,
     * along with pagination metadata such as total pages, page size, and total elements.
     * </p>
     *
     * @param feedbacks     The list of feedback details.
     * @param totalPages    The total number of pages in the feedback report.
     * @param pageSize      The number of feedback entries per page.
     * @param totalElements The total number of feedback records.
     * @return A constructed FeedbackReportResponse object.
     */
    private FeedbackReportResponse buildFeedbackReportResponse(
            List<FeedbackDetailResponse> feedbacks,
            int totalPages,
            int pageSize,
            long totalElements
    ) {
        return FeedbackReportResponse.builder()
                .feedbacks(feedbacks)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .build();
    }

    /**
     * Retrieves the average rating and rating distribution for a car owner.
     * <p>
     * This method fetches the car owner's ID from the security context, retrieves their car IDs,
     * and calculates the average rating based on feedback data. It also counts the number of feedback
     * entries for each rating level (1-5 stars) to provide a distribution of ratings.
     * </p>
     * <p>
     * If the car owner has no cars, the method returns a default response with an average rating of 0.0
     * and zero counts for all rating levels.
     * </p>
     *
     * @return A {@link RatingResponse} containing the average rating and a map of rating counts.
     */
    @Transactional(readOnly = true)
    public RatingResponse getAverageRatingAndCountByCarOwner() {
        // Get the current authenticated car owner's ID
        String ownerId = SecurityUtil.getCurrentAccountId();

        // Retrieve the list of car IDs owned by the car owner
        List<String> carIds = carRepository.findCarIdsByOwnerId(ownerId);

        // If the owner has no registered cars, return a default response
        if (carIds.isEmpty()) {
            return RatingResponse.builder()
                    .averageRatingByOwner(0.0)
                    .ratingCounts(Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L))
                    .build();
        }

        // Calculate the average rating of the car owner based on all their cars' feedback
        Double averageRatingByOwner = feedbackRepository.calculateAverageRatingByOwner(carIds);

        // Ensure the rating is rounded to 2 decimal places, or default to 0.0 if null
        averageRatingByOwner = (averageRatingByOwner != null)
                ? BigDecimal.valueOf(averageRatingByOwner).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Count the number of feedback entries grouped by rating (1-5 stars)
        List<Object[]> ratingCountData = feedbackRepository.countFeedbackByRating(carIds);
        Map<Integer, Long> ratingCounts = ratingCountData.stream()
                .collect(Collectors.toMap(
                        data -> ((Number) data[0]).intValue(), // Extract rating value (1-5)
                        data -> ((Number) data[1]).longValue(), // Extract count for that rating
                        (oldValue, newValue) -> oldValue // Handle duplicates by keeping the first occurrence
                ));

        // Ensure all rating levels (1-5 stars) are present in the map, defaulting to zero if missing
        for (int i = 1; i <= 5; i++) {
            ratingCounts.putIfAbsent(i, 0L);
        }

        // Build and return the response containing the average rating and rating counts
        return RatingResponse.builder()
                .averageRatingByOwner(averageRatingByOwner)
                .ratingCounts(ratingCounts)
                .build();
    }
}