package com.mp.karental.service;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Feedback;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.FeedbackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for handling feedback operations.
 *
 * @version 1.3
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService {
    FeedbackRepository feedbackRepository;
    BookingRepository bookingRepository;
    CarRepository carRepository;

    public void addFeedback(FeedbackRequest request) {
        String bookingNumber = request.getBookingId(); // Dùng bookingNumber thay vì bookingId
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);

        if (booking == null) {
            log.warn("Không tìm thấy booking với số {}", bookingNumber);
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }

        // Kiểm tra nếu feedback quá hạn 30 ngày sau drop-off
        if (booking.getDropOffTime().plusDays(30).isBefore(LocalDateTime.now())) {
            log.warn("Feedback quá hạn 30 ngày sau drop-off cho booking {}", bookingNumber);
            throw new AppException(ErrorCode.FEEDBACK_EXPIRED);
        }

        // Kiểm tra giới hạn độ dài review
        if (request.getComment().length() > 250) {
            log.warn("Review vượt quá 250 ký tự: {}", request.getComment());
            throw new AppException(ErrorCode.FEEDBACK_TOO_LONG);
        }

        Feedback feedback = Feedback.builder()
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        feedbackRepository.save(feedback);
        log.info("Feedback được thêm thành công cho booking {}", bookingNumber);

        // Cập nhật rating của xe
        updateCarRating(booking.getCar().getId());
    }

    private void updateCarRating(String carId) {
        Double avgRating = feedbackRepository.findAverageRatingByCarId(carId);
        if (avgRating != null) {
            log.info("Cập nhật rating cho xe {} thành {}", carId, avgRating);
        } else {
            log.info("Không tìm thấy feedback nào cho xe {}", carId);
        }
    }
}
