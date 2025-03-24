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

        // Validate feedback length (max 2000 characters)
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
     * Retrieves a paginated feedback report based on the provided filters.
     * <p>
     * This method allows fetching feedback for either car owners or customers based on the `includeRating` flag.
     * If `includeRating` is true, it retrieves feedback for cars owned by the authenticated user.
     * If `includeRating` is false, it retrieves feedback for cars the user has previously rated.
     * </p>
     * <p>
     * The method also calculates the total number of feedback entries for the selected rating filter
     * and determines pagination details such as total pages and total elements.
     * </p>
     *
     * @param ratingFilter  The rating to filter feedback (1-5). If 0, fetches all feedback.
     * @param page          The page number (zero-based index).
     * @param size          The number of feedback entries per page.
     * @param includeRating Whether to fetch feedback for the authenticated user's owned cars (true) or rated cars (false).
     * @return A {@link FeedbackReportResponse} containing feedback details and pagination metadata.
     */
    @Transactional(readOnly = true)
    public FeedbackReportResponse getFilteredFeedbackReportCommon(int ratingFilter, int page, int size, boolean includeRating) {
        log.info("Fetching feedback report for ratingFilter={}, page={}, size={}, includeRating={}", ratingFilter, page, size, includeRating);

        // Retrieve the authenticated user's ID
        String userId = SecurityUtil.getCurrentAccountId();

        // Fetch the list of car IDs based on user role (owner or customer)
        List<String> relatedCarIds = includeRating
                ? carRepository.findCarIdsByOwnerId(userId) // Fetch cars owned by the user
                : carRepository.findCarIdsByCustomerId(userId); // Fetch cars the user has rated

        if (relatedCarIds.isEmpty()) {
            log.warn("No relevant cars found for userId={}", userId);
            return buildFeedbackReportResponse(Collections.emptyList(), 0, size, 0);
        }

        // Retrieve the count of feedback per rating level
        Map<Integer, Long> ratingCounts = getRatingCountsByCarIds(relatedCarIds);

        // Calculate the total number of feedback entries based on the rating filter
        long totalElements = (ratingFilter > 0) ? ratingCounts.getOrDefault(ratingFilter, 0L)
                : ratingCounts.values().stream().mapToLong(Long::longValue).sum();

        // Calculate the total number of pages
        int totalPages = (size > 0 && totalElements > 0) ? (int) Math.ceil((double) totalElements / size) : 0;

        // Define pagination and sorting by creation date (descending order)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));

        // Fetch paginated feedback based on the rating filter
        Page<Feedback> feedbackPage = (ratingFilter > 0)
                ? feedbackRepository.findByCarIdsAndRating(relatedCarIds, ratingFilter, pageRequest)
                : feedbackRepository.findByCarIds(relatedCarIds, pageRequest);

        // Convert feedback entities to response DTOs
        List<FeedbackDetailResponse> feedbackDetails = feedbackMapper.toFeedbackDetailResponseList(feedbackPage.getContent());
        feedbackDetails.forEach(detail -> detail.setCarImageFrontUrl(fileService.getFileUrl(detail.getCarImageFrontUrl())));

        // Build and return the response
        return buildFeedbackReportResponse(feedbackDetails, totalPages, size, totalElements);
    }

    /**
     * Retrieves a paginated feedback report for a car owner.
     *
     * @param ratingFilter The rating to filter feedback (1-5). If 0, fetches all feedback.
     * @param page         The page number (zero-based index).
     * @param size         The number of feedback entries per page.
     * @return A {@link FeedbackReportResponse} containing feedback details and pagination metadata.
     */
    @Transactional(readOnly = true)
    public FeedbackReportResponse getOwnerFeedbackReport(int ratingFilter, int page, int size) {
        return getFilteredFeedbackReportCommon(ratingFilter, page, size, true);
    }

    /**
     * Retrieves a paginated feedback report for a customer.
     *
     * @param ratingFilter The rating to filter feedback (1-5). If 0, fetches all feedback.
     * @param page         The page number (zero-based index).
     * @param size         The number of feedback entries per page.
     * @return A {@link FeedbackReportResponse} containing feedback details and pagination metadata.
     */
    @Transactional(readOnly = true)
    public FeedbackReportResponse getCustomerFeedbackReport(int ratingFilter, int page, int size) {
        return getFilteredFeedbackReportCommon(ratingFilter, page, size, false);
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

        // Get the number of feedbacks by rating from a common method
        Map<Integer, Long> ratingCounts = getRatingCountsByCarIds(carIds);

        // Build and return the response containing the average rating and rating counts
        return RatingResponse.builder()
                .averageRatingByOwner(averageRatingByOwner)
                .ratingCounts(ratingCounts)
                .build();
    }

    /**
     * Retrieves the count of feedback for each rating level (1-5) for a given list of car IDs.
     *
     * @param carIds The list of car IDs to retrieve feedback counts.
     * @return A map where the key is the rating (1-5) and the value is the count of feedback entries.
     */
    private Map<Integer, Long> getRatingCountsByCarIds(List<String> carIds) {

        List<Object[]> ratingCountData = feedbackRepository.countFeedbackByRating(carIds);
        Map<Integer, Long> ratingCounts = ratingCountData.stream().collect(Collectors.toMap(
                data -> ((Number) data[0]).intValue(), // Rating (1-5)
                data -> ((Number) data[1]).longValue(), // Number of feedback
                (oldValue, newValue) -> oldValue // Avoid duplicate key
        ));

        // Ensure enough key from 1 to 5, default value 0 when don't have data
        for (int i = 1; i <= 5; i++) {
            ratingCounts.putIfAbsent(i, 0L);
        }

        return ratingCounts;
    }

}