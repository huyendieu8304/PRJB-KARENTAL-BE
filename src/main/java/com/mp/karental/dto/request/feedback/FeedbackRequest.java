package com.mp.karental.dto.request.feedback;

import com.mp.karental.validation.RequiredField;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "request.car.FeedbackRequest",description = "Request DTO for submitting feedback on a booking.")
public class FeedbackRequest {

    @RequiredField(message = "Booking ID")
    @Schema(description = "Unique identifier of the booking associated with this feedback", example = "b123456")
    String bookingId;

    @RequiredField(message = "Rating")
    @Min(value = 1, message = "INVALID_RATING_RANGE")
    @Max(value = 5, message = "INVALID_RATING_RANGE")
    @Schema(description = "Rating given by the user, ranging from 1 to 5", example = "5")
    int rating;

    @Size(max = 2000, message = "INVALID_COMMENT_LENGTH")
    @Schema(description = "Optional comment provided by the user (max 2000 characters)", example = "Great service, highly recommended!")
    String comment;
}
