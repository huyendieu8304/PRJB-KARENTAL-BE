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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;

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

    @Mock
    private FileService fileService;

    private final String MOCK_USER_ID = "user123";
    private final String MOCK_CAR_ID = "car123";

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackRequest validRequest;
    private Booking completedBooking;
    private Feedback feedback;

    @Mock
    private CarRepository carRepository;


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

    @Test
    void getFeedbackByCarId_ShouldReturnFeedbackList_WhenFeedbackExists() {
        String carId = "CAR123";
        List<Feedback> feedbackList = List.of(feedback);
        when(feedbackRepository.findByCarId(carId)).thenReturn(feedbackList);
        when(feedbackMapper.toFeedbackResponse(any(Feedback.class))).thenReturn(new FeedbackResponse());

        List<FeedbackResponse> responses = feedbackService.getFeedbackByCarId(carId);

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        verify(feedbackRepository).findByCarId(carId);
    }

    @Test
    void getFeedbackByCarId_ShouldReturnEmptyList_WhenNoFeedbackExists() {
        String carId = "CAR123";
        when(feedbackRepository.findByCarId(carId)).thenReturn(Collections.emptyList());

        List<FeedbackResponse> responses = feedbackService.getFeedbackByCarId(carId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(feedbackRepository).findByCarId(carId);
    }


    @Test
    void getFeedbackByCarId_ShouldReturnFeedbackList() {
        // Given
        String carId = "car123";
        List<Feedback> feedbackList = List.of(new Feedback(), new Feedback());
        List<FeedbackResponse> feedbackResponses = List.of(new FeedbackResponse(), new FeedbackResponse());

        when(feedbackRepository.findByCarId(carId)).thenReturn(feedbackList);
        when(feedbackMapper.toFeedbackResponse(any())).thenReturn(new FeedbackResponse());

        // When
        List<FeedbackResponse> result = feedbackService.getFeedbackByCarId(carId);

        // Then
        assertEquals(2, result.size());
        verify(feedbackRepository, times(1)).findByCarId(carId);
    }

    @Test
    void getFilteredFeedbackReportCommon_ShouldReturnEmptyResponse_WhenNoCarsFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            // Giả lập user ID từ SecurityUtil
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            // Giả lập carRepository trả về danh sách trống
            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(Collections.emptyList());

            // Gọi method cần test
            FeedbackReportResponse response = feedbackService.getFilteredFeedbackReportCommon(0, 0, 10, true);

            // Xác minh kết quả
            assertNotNull(response);
            assertEquals(0, response.getTotalElements());
        }
    }

    @Test
    void getFilteredFeedbackReportCommon_ShouldReturnFeedback_WhenCarsExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            // Giả lập carRepository trả về danh sách có xe
            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));

            // Giả lập repository trả về danh sách feedback có sẵn
            Feedback mockFeedback = new Feedback();
            Page<Feedback> feedbackPage = new PageImpl<>(List.of(mockFeedback));
            when(feedbackRepository.findByCarIds(List.of(MOCK_CAR_ID), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createAt"))))
                    .thenReturn(feedbackPage);

            // Giả lập mapper chuyển đổi feedback thành DTO
            FeedbackDetailResponse mockResponse = new FeedbackDetailResponse();
            when(feedbackMapper.toFeedbackDetailResponseList(anyList())).thenReturn(List.of(mockResponse));

            // Gọi method cần test
            FeedbackReportResponse response = feedbackService.getFilteredFeedbackReportCommon(0, 0, 10, true);

            // Xác minh kết quả
            assertNotNull(response);
            assertEquals(1, response.getFeedbacks().size());
        }
    }

    @Test
    void getOwnerFeedbackReport_ShouldReturnFeedbackReport_WhenCarsExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            // Giả lập carRepository trả về danh sách có xe của owner
            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));

            // Giả lập repository trả về danh sách feedback có sẵn
            Feedback mockFeedback = new Feedback();
            Page<Feedback> feedbackPage = new PageImpl<>(List.of(mockFeedback));
            when(feedbackRepository.findByCarIds(List.of(MOCK_CAR_ID), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createAt"))))
                    .thenReturn(feedbackPage);

            // Giả lập mapper chuyển đổi feedback thành DTO
            FeedbackDetailResponse mockResponse = new FeedbackDetailResponse();
            when(feedbackMapper.toFeedbackDetailResponseList(anyList())).thenReturn(List.of(mockResponse));

            // Gọi method cần test
            FeedbackReportResponse response = feedbackService.getOwnerFeedbackReport(0, 0, 10);

            // Xác minh kết quả
            assertNotNull(response);
            assertEquals(1, response.getFeedbacks().size());
        }
    }

    @Test
    void getCustomerFeedbackReport_ShouldReturnFeedbackReport_WhenCarsExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            // Giả lập carRepository trả về danh sách có xe của customer
            when(carRepository.findCarIdsByCustomerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));

            // Giả lập repository trả về danh sách feedback có sẵn
            Feedback mockFeedback = new Feedback();
            Page<Feedback> feedbackPage = new PageImpl<>(List.of(mockFeedback));
            when(feedbackRepository.findByCarIds(List.of(MOCK_CAR_ID), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createAt"))))
                    .thenReturn(feedbackPage);

            // Giả lập mapper chuyển đổi feedback thành DTO
            FeedbackDetailResponse mockResponse = new FeedbackDetailResponse();
            when(feedbackMapper.toFeedbackDetailResponseList(anyList())).thenReturn(List.of(mockResponse));

            // Gọi method cần test
            FeedbackReportResponse response = feedbackService.getCustomerFeedbackReport(0, 0, 10);

            // Xác minh kết quả
            assertNotNull(response);
            assertEquals(1, response.getFeedbacks().size());
        }
    }

    @Test
    void getAverageRatingAndCountByCarOwner_ShouldReturnDefaultResponse_WhenNoCarsExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            // Giả lập carRepository trả về danh sách rỗng (không có xe)
            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(Collections.emptyList());

            // Gọi method cần test
            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            // Xác minh kết quả
            assertNotNull(response);
            assertEquals(0.0, response.getAverageRatingByOwner());
            assertEquals(0L, response.getRatingCounts().get(5));
        }
    }

    @Test
    void getAverageRatingAndCountByCarOwner_ShouldReturnCorrectRating_WhenDataExists() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));
            when(feedbackRepository.calculateAverageRatingByOwner(List.of(MOCK_CAR_ID))).thenReturn(4.5);

            List<Object[]> ratingData = List.of(
                    new Object[]{5, 10L},
                    new Object[]{4, 5L},
                    new Object[]{3, 2L}
            );
            when(feedbackRepository.countFeedbackByRating(List.of(MOCK_CAR_ID))).thenReturn(ratingData);

            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            assertNotNull(response);
            assertEquals(4.5, response.getAverageRatingByOwner());
            assertEquals(10L, response.getRatingCounts().get(5));
            assertEquals(5L, response.getRatingCounts().get(4));
            assertEquals(2L, response.getRatingCounts().get(3));
            assertEquals(0L, response.getRatingCounts().get(2));
            assertEquals(0L, response.getRatingCounts().get(1));
        }
    }

    @Test
    void getAverageRatingAndCountByCarOwner_ShouldReturnDefaultValues_WhenNoCarsExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(Collections.emptyList());

            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            assertNotNull(response);
            assertEquals(0.0, response.getAverageRatingByOwner());
            assertEquals(0L, response.getRatingCounts().get(5));
            assertEquals(0L, response.getRatingCounts().get(4));
            assertEquals(0L, response.getRatingCounts().get(3));
            assertEquals(0L, response.getRatingCounts().get(2));
            assertEquals(0L, response.getRatingCounts().get(1));
        }
    }

    @Test
    void getAverageRatingAndCountByCarOwner_ShouldReturnZeroAverage_WhenAverageIsNull() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));
            when(feedbackRepository.calculateAverageRatingByOwner(List.of(MOCK_CAR_ID))).thenReturn(null);

            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            assertNotNull(response);
            assertEquals(0.0, response.getAverageRatingByOwner());
        }
    }

    @Test
    void getAverageRatingAndCountByCarOwner_ShouldHandleEmptyFeedbackCounts() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));
            when(feedbackRepository.calculateAverageRatingByOwner(List.of(MOCK_CAR_ID))).thenReturn(3.5);
            when(feedbackRepository.countFeedbackByRating(List.of(MOCK_CAR_ID))).thenReturn(Collections.emptyList());

            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            assertNotNull(response);
            assertEquals(3.5, response.getAverageRatingByOwner());
            assertEquals(0L, response.getRatingCounts().get(1));
            assertEquals(0L, response.getRatingCounts().get(2));
            assertEquals(0L, response.getRatingCounts().get(3));
            assertEquals(0L, response.getRatingCounts().get(4));
            assertEquals(0L, response.getRatingCounts().get(5));
        }
    }

    @Test
    void getAverageRatingAndCountByCarOwner_ShouldHandleLargeNumberOfFeedbacks() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);

            when(carRepository.findCarIdsByOwnerId(MOCK_USER_ID)).thenReturn(List.of(MOCK_CAR_ID));
            when(feedbackRepository.calculateAverageRatingByOwner(List.of(MOCK_CAR_ID))).thenReturn(4.2);

            List<Object[]> ratingData = List.of(
                    new Object[]{5, 1_000_000L},
                    new Object[]{4, 500_000L},
                    new Object[]{3, 100_000L}
            );
            when(feedbackRepository.countFeedbackByRating(List.of(MOCK_CAR_ID))).thenReturn(ratingData);

            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            assertNotNull(response);
            assertEquals(4.2, response.getAverageRatingByOwner());
            assertEquals(1_000_000L, response.getRatingCounts().get(5));
            assertEquals(500_000L, response.getRatingCounts().get(4));
            assertEquals(100_000L, response.getRatingCounts().get(3));
            assertEquals(0L, response.getRatingCounts().get(2));
            assertEquals(0L, response.getRatingCounts().get(1));
        }
    }
    @Test
    public void testGetAverageRatingAndCountByCarOwner() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(MOCK_USER_ID);
            // Dữ liệu giả lập carIds và feedback
            List<String> carIds = Arrays.asList("car1", "car2");

            // Giả lập dữ liệu trả về từ carRepository
            when(carRepository.findCarIdsByOwnerId(anyString())).thenReturn(carIds);

            // Giả lập dữ liệu trả về từ feedbackRepository
            when(feedbackRepository.calculateAverageRatingByOwner(carIds)).thenReturn(4.2);

            // Giả lập feedback count (sử dụng phương thức private)
            List<Object[]> ratingCountData = Arrays.asList(
                    new Object[]{1, 5L},  // Rating 1, count 5
                    new Object[]{1, 3L},  // Rating 1, count 5
                    new Object[]{2, 10L}, // Rating 2, count 10
                    new Object[]{3, 7L}   // Rating 3, count 7
            );
            when(feedbackRepository.countFeedbackByRating(carIds)).thenReturn(ratingCountData);

            // Gọi hàm public getAverageRatingAndCountByCarOwner
            RatingResponse response = feedbackService.getAverageRatingAndCountByCarOwner();

            // Kiểm tra giá trị trong RatingResponse
            assertEquals(4.2, response.getAverageRatingByOwner(), 0.01); // Kiểm tra giá trị rating
            assertEquals(5L, response.getRatingCounts().get(1).longValue()); // Kiểm tra count cho rating 1
            assertEquals(10L, response.getRatingCounts().get(2).longValue()); // Kiểm tra count cho rating 2
            assertEquals(7L, response.getRatingCounts().get(3).longValue()); // Kiểm tra count cho rating 3
            assertEquals(0L, response.getRatingCounts().get(4).longValue()); // Kiểm tra rating 4, mặc định 0L
            assertEquals(0L, response.getRatingCounts().get(5).longValue()); // Kiểm tra rating 5, mặc định 0L
        }
    }

}



