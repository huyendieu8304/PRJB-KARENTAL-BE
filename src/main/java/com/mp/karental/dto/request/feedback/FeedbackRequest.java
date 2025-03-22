package com.mp.karental.dto.request.feedback;

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
    @NotNull(message = "Booking ID is required")
    String bookingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    int rating;

    @Size(max = 250, message = "Comment must not exceed 250 characters")
    String comment;

}
