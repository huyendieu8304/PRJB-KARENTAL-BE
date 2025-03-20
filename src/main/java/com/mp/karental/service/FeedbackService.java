package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Feedback;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.FeedbackMapper;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.FeedbackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling feedback operations.
 *
 * @author AnhPH9
 * @version 1.0
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

    /**
     * Adds feedback (rating + comment) for a completed booking.
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
            throw new AppException(ErrorCode.FEEDBACK_EXPIRED);
        }

        // Validate feedback length (max 250 characters)
        if (request.getComment() != null && request.getComment().length() > 250) {
            throw new AppException(ErrorCode.FEEDBACK_TOO_LONG);
        }

        // Convert request DTO to Feedback entity
        Feedback feedback = feedbackMapper.toFeedback(request);
        feedback.setBooking(booking);

        // Save feedback to the database
        feedbackRepository.save(feedback);
        FeedbackResponse response = feedbackMapper.toFeedbackResponse(feedback);
        response.setCreatedAt(LocalDateTime.now());
        // Return the response after saving
        return response;
    }

    /**
     * Retrieves all feedback for a specific booking.
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
}