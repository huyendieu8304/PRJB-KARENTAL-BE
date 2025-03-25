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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FeedbackMapper feedbackMapper;

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackRequest validRequest;
    private Booking completedBooking;
    private Feedback feedback;

    @BeforeEach
    void setUp() {
        validRequest = new FeedbackRequest();
        validRequest.setBookingId("BK123");
        validRequest.setRating(5);
        validRequest.setComment("Great service!");

        completedBooking = new Booking();
        completedBooking.setBookingNumber("BK123");
        completedBooking.setStatus(EBookingStatus.COMPLETED);
        completedBooking.setDropOffTime(LocalDateTime.now().minusDays(10));

        feedback = new Feedback();
        feedback.setBooking(completedBooking);
        feedback.setComment("Great service!");
        feedback.setRating(5);
    }

    @Test
    void addFeedback_ShouldSaveFeedback_WhenValidRequest() {
        when(bookingRepository.findBookingByBookingNumber(validRequest.getBookingId())).thenReturn(completedBooking);
        when(feedbackRepository.existsById(validRequest.getBookingId())).thenReturn(false);
        when(feedbackMapper.toFeedback(validRequest)).thenReturn(feedback);
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);
        when(feedbackMapper.toFeedbackResponse(feedback)).thenReturn(new FeedbackResponse());

        FeedbackResponse response = feedbackService.addFeedback(validRequest);

        assertNotNull(response);
        verify(feedbackRepository).save(feedback);
    }

    /** NEGATIVE TEST CASES **/

    @Test
    void addFeedback_ShouldThrowException_WhenBookingNotCompleted() {
        completedBooking.setStatus(EBookingStatus.IN_PROGRESS);
        when(bookingRepository.findBookingByBookingNumber(validRequest.getBookingId())).thenReturn(completedBooking);

        AppException exception = assertThrows(AppException.class, () -> feedbackService.addFeedback(validRequest));
        assertEquals(ErrorCode.BOOKING_NOT_COMPLETED, exception.getErrorCode());
    }

    @Test
    void addFeedback_ShouldThrowException_WhenFeedbackAlreadyExists() {
        when(bookingRepository.findBookingByBookingNumber(validRequest.getBookingId())).thenReturn(completedBooking);
        when(feedbackRepository.existsById(validRequest.getBookingId())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> feedbackService.addFeedback(validRequest));
        assertEquals(ErrorCode.FEEDBACK_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void addFeedback_ShouldThrowException_WhenFeedbackTimeExpired() {
        completedBooking.setDropOffTime(LocalDateTime.now().minusDays(40));
        when(bookingRepository.findBookingByBookingNumber(validRequest.getBookingId())).thenReturn(completedBooking);
        when(feedbackRepository.existsById(validRequest.getBookingId())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> feedbackService.addFeedback(validRequest));
        assertEquals(ErrorCode.FEEDBACK_TIME_EXPIRED, exception.getErrorCode());
    }

    @Test
    void addFeedback_ShouldThrowException_WhenCommentTooLong() {
        validRequest.setComment("A".repeat(2001));
        when(bookingRepository.findBookingByBookingNumber(validRequest.getBookingId())).thenReturn(completedBooking);
        when(feedbackRepository.existsById(validRequest.getBookingId())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> feedbackService.addFeedback(validRequest));
        assertEquals(ErrorCode.FEEDBACK_TOO_LONG, exception.getErrorCode());
    }

    @Test
    void addFeedback_ShouldHandleNullComment() {
        validRequest.setComment(null);
        when(bookingRepository.findBookingByBookingNumber(validRequest.getBookingId())).thenReturn(completedBooking);
        when(feedbackRepository.existsById(validRequest.getBookingId())).thenReturn(false);
        when(feedbackMapper.toFeedback(validRequest)).thenReturn(feedback);
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);
        when(feedbackMapper.toFeedbackResponse(feedback)).thenReturn(new FeedbackResponse());

        FeedbackResponse response = feedbackService.addFeedback(validRequest);

        assertNotNull(response);
        verify(feedbackRepository).save(feedback);
    }

    @Test
    void getFeedbackByBookingId_ShouldReturnFeedback_WhenExists() {
        when(feedbackRepository.findByBookingNumber("BK123")).thenReturn(Optional.of(feedback));
        when(feedbackMapper.toFeedbackResponse(feedback)).thenReturn(new FeedbackResponse());

        FeedbackResponse response = feedbackService.getFeedbackByBookingId("BK123");

        assertNotNull(response);
        verify(feedbackRepository).findByBookingNumber("BK123");
    }

    @Test
    void getFeedbackByBookingId_ShouldReturnNull_WhenNotExists() {
        when(feedbackRepository.findByBookingNumber("BK123")).thenReturn(Optional.empty());

        FeedbackResponse response = feedbackService.getFeedbackByBookingId("BK123");

        assertNull(response);
    }


}
