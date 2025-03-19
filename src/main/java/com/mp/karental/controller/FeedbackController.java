package com.mp.karental.controller;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class FeedbackController {

    FeedbackService feedbackService;

    /**
     * API để khách hàng gửi đánh giá sau khi thuê xe.
     *
     * @param request FeedbackRequest chứa thông tin đánh giá
     * @return ApiResponse chứa thông tin phản hồi sau khi gửi đánh giá
     */
    @PostMapping("/customer")
    public ApiResponse<Void> addFeedback(@Valid @RequestBody FeedbackRequest request) {
        log.info("Received feedback request: {}", request);
        feedbackService.addFeedback(request);
        return ApiResponse.<Void>builder()
                .message("Feedback submitted successfully.")
                .build();
    }
}
