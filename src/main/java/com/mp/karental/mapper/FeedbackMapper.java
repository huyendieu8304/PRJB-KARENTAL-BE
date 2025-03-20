package com.mp.karental.mapper;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Feedback entity and DTOs.
 * AnhPH9
 *
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    /**
     * Maps FeedbackRequest to Feedback entity.
     */
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    Feedback toFeedback(FeedbackRequest request);

    /**
     * Maps Feedback entity to FeedbackResponse DTO.
     */
    @Mapping(target = "bookingId", expression = "java(feedback.getBooking() != null ? feedback.getBooking().getBookingNumber() : null)")
    @Mapping(target = "createdAt", source = "createAt")
    FeedbackResponse toFeedbackResponse(Feedback feedback);
}
