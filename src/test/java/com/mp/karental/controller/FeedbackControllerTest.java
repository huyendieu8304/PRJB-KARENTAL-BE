package com.mp.karental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.karental.KarentalApplication;
import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.feedback.FeedbackReportResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.dto.response.feedback.RatingResponse;
import com.mp.karental.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = KarentalApplication.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedbackService feedbackService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testGiveRating_Success() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setBookingId("BK123");
        request.setRating(5);
        request.setComment("Excellent service");

        FeedbackResponse response = FeedbackResponse.builder()
                .bookingId("BK123")
                .rating(5)
                .comment("Excellent service")
                .createdAt(LocalDateTime.now())
                .reviewerName("John Doe")
                .build();

        when(feedbackService.addFeedback(any(FeedbackRequest.class))).thenReturn(response);

        mockMvc.perform(post("/feedback/customer/give-rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingId").value("BK123"))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Excellent service"));
    }

    @Test
    void testGetFeedbackByBookingId_Success() throws Exception {
        FeedbackResponse response = FeedbackResponse.builder()
                .bookingId("BK123")
                .rating(5)
                .comment("Great car!")
                .createdAt(LocalDateTime.now())
                .reviewerName("Alice")
                .build();

        when(feedbackService.getFeedbackByBookingId("BK123")).thenReturn(response);

        mockMvc.perform(get("/feedback/customer/view-ratings/BK123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingId").value("BK123"))
                .andExpect(jsonPath("$.data.comment").value("Great car!"));
    }

    @Test
    void testGetFeedbackByCarId_Success() throws Exception {
        when(feedbackService.getFeedbackByCarId("CAR123"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/feedback/car/CAR123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetOwnerFeedbackReport_Success() throws Exception {
        FeedbackReportResponse reportResponse = FeedbackReportResponse.builder()
                .feedbacks(Collections.emptyList())
                .totalPages(1)
                .pageSize(10)
                .totalElements(0)
                .build();

        when(feedbackService.getOwnerFeedbackReport(0, 0, 10)).thenReturn(reportResponse);

        mockMvc.perform(get("/feedback/car-owner/my-feedbacks")
                        .param("ratingFilter", "0")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    void testGetRatingFeedbackReport_Success() throws Exception {
        Map<Integer, Long> ratingCounts = new HashMap<>();
        ratingCounts.put(5, 10L);
        ratingCounts.put(4, 5L);

        RatingResponse response = RatingResponse.builder()
                .ratingCounts(ratingCounts)
                .averageRatingByOwner(4.5)
                .build();

        when(feedbackService.getAverageRatingAndCountByCarOwner()).thenReturn(response);

        mockMvc.perform(get("/feedback/car-owner/rating")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRatingByOwner").value(4.5))
                .andExpect(jsonPath("$.data.ratingCounts['5']").value(10))
                .andExpect(jsonPath("$.data.ratingCounts['4']").value(5));
    }

    @Test
    void testGetCustomerFeedbackReport_Success() throws Exception {
        FeedbackReportResponse response = FeedbackReportResponse.builder()
                .feedbacks(Collections.emptyList())
                .totalPages(2)
                .pageSize(5)
                .totalElements(10)
                .build();

        when(feedbackService.getCustomerFeedbackReport(0, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/feedback/customer/view-feedbacks")
                        .param("ratingFilter", "0")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }
}
