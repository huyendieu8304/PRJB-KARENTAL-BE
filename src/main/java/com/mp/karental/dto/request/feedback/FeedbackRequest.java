package com.mp.karental.dto.request.feedback;

import com.mp.karental.validation.RequiredField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request payload for adding a new feedback.
 * <p>
 * This class encapsulates the necessary data required to create a new feedback,
 * </p>
 * @author ANHHP9
 * @version 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FeedbackRequest {
    @RequiredField(message = "Booking ID")
    String bookingId;

    @RequiredField(message = "Rating")
    @Min(value = 1, message = "INVALID_RATING_RANGE")
    @Max(value = 5, message = "INVALID_RATING_RANGE")
    int rating;

    @Size(max = 2000, message = "INVALID_COMMENT_LENGTH")
    String comment;

}
