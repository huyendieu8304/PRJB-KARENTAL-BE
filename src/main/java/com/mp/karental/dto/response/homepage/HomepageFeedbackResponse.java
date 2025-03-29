package com.mp.karental.dto.response.homepage;

import com.mp.karental.dto.response.feedback.SimpleFeedbackResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * DTO for Homepage response.
 * Includes latest 4 five-star feedbacks.
 *
 * @author AnhHP9
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class HomepageFeedbackResponse {
    List<SimpleFeedbackResponse> latestFiveStarFeedbacks;
}
