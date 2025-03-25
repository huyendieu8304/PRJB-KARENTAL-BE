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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is a class used to test FileService, service used with AWS S3
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile file;
    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private FileService fileService;

    private static String bucketName;
    private static String key;


    @BeforeEach
    public void setUp() {
        bucketName = "test-bucket";
        key = "test-file.txt";
    }

    @Test
    public void testUploadFile_Success() throws IOException {
        byte[] fileBytes = "Test file content".getBytes();
        when(file.getBytes()).thenReturn(fileBytes);

        // Use ArgumentCaptor to catch PutObjectRequest, which create internal in the method
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // Mock s3Client.putObject to check parameter
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(null);

        // Call the tested method
        fileService.uploadFile(file, key);

        // Assert
        // check s3Client.putObject is called?
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));


        //check value of PutObjectRequest catched
        PutObjectRequest capturedRequest = captor.getValue();
//        assertEquals(key, capturedRequest.key());
    }

    @Test
    public void testUploadFile_Failure() throws IOException {
        // Arrange
        when(file.getBytes()).thenThrow(new IOException("Test exception"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> fileService.uploadFile(file, key));
        assertEquals(ErrorCode.UPLOAD_OBJECT_TO_S3_FAIL, exception.getErrorCode());
    }

    @Test
    public void testGetPresignedUrl_Success() throws MalformedURLException {

        String expectedUrl = "http://example.com/signed-url";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String actualUrl = fileService.getFileUrl(key);

        
        assertEquals(expectedUrl, actualUrl);

        //Check parameter in GetObjectPresignRequest
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        GetObjectPresignRequest capturedRequest = captor.getValue();

        // duration of the url
        assertEquals(Duration.ofMinutes(30), capturedRequest.signatureDuration());

        GetObjectRequest getObjectRequest = capturedRequest.getObjectRequest();
        assertEquals(key, getObjectRequest.key());
    }

    @Test
    void getFileExtension_ShouldReturnExtension_WhenFileNameHasExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("example.jpg");

        String extension = fileService.getFileExtension(file);

        assertEquals(".jpg", extension);
    }

    @Test
    void getFileExtension_ShouldReturnEmptyString_WhenFileNameHasNoExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("example");

        String extension = fileService.getFileExtension(file);

        assertEquals("", extension);
    }

    @Test
    void getFileExtension_ShouldReturnEmptyString_WhenFileNameIsNull() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        String extension = fileService.getFileExtension(file);

        assertEquals("", extension);
    }

    @ExtendWith(MockitoExtension.class)
    static
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

        /** POSITIVE TEST CASES **/

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

        /** EDGE CASES **/

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
        void getFeedbackByBookingId_ShouldReturnNull_WhenNotExists() {
            when(feedbackRepository.findByBookingNumber("BK123")).thenReturn(Optional.empty());

            FeedbackResponse response = feedbackService.getFeedbackByBookingId("BK123");

            assertNull(response);
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
        void testGetFeedbackByCarId() {
            List<Feedback> feedbackList = List.of(new Feedback(), new Feedback());
            when(feedbackRepository.findByCarId("car123")).thenReturn(feedbackList);
            when(feedbackMapper.toFeedbackResponse(any())).thenReturn(new FeedbackResponse());

            List<FeedbackResponse> result = feedbackService.getFeedbackByCarId("car123");
            assertEquals(2, result.size());

            when(feedbackRepository.findByCarId("invalid_car")).thenReturn(Collections.emptyList());
            assertTrue(feedbackService.getFeedbackByCarId("invalid_car").isEmpty());
        }


    }
}