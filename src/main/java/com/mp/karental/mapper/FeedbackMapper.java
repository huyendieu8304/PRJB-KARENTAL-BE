package com.mp.karental.mapper;

import com.mp.karental.dto.request.feedback.FeedbackRequest;
import com.mp.karental.dto.response.feedback.FeedbackDetailResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting between Feedback entity and DTOs.
 * This interface defines mapping methods to convert between Feedback entities
 * and their corresponding Data Transfer Objects (DTOs) using MapStruct.
 * Author: AnhPH9
 * Version: 1.0
 */
@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    /**
     * Converts a FeedbackRequest DTO to a Feedback entity.
     * - Ignores the `booking` field since it's managed separately.
     * - Ignores `id` since it's generated automatically.
     * - Ignores `createAt` and `updateAt` as they are set automatically when persisting data.
     *
     * @param request The feedback request DTO.
     * @return The mapped Feedback entity.
     */
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    Feedback toFeedback(FeedbackRequest request);

    /**
     * Converts a Feedback entity to a FeedbackResponse DTO.
     * - Retrieves `bookingId` from the associated booking if available.
     * - Maps fields such as `rating`, `comment`, `createdAt`, and `reviewerName`.
     * - Extracts the reviewer's full name from the profile.
     *
     * @param feedback The Feedback entity.
     * @return The mapped FeedbackResponse DTO.
     */
    @Mapping(target = "bookingId", expression = "java(feedback.getBooking() != null ? feedback.getBooking().getBookingNumber() : null)")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "createdAt", source = "createAt")
    @Mapping(target = "reviewerName", source = "booking.account.profile.fullName")
    FeedbackResponse toFeedbackResponse(Feedback feedback);

    // UC25 - Detailed feedback report for car owners

    /**
     * Converts a Feedback entity to a FeedbackDetailResponse DTO.
     * - Retrieves `bookingId`, `rating`, `comment`, `createdAt`, and `reviewerName`.
     * - Extracts vehicle details from the associated booking, including:
     *   - Pickup and drop-off times.
     *   - Car brand and model.
     *   - Image URLs for different car angles.
     *
     * @param feedback The Feedback entity.
     * @return The mapped FeedbackDetailResponse DTO.
     */
    @Mapping(target = "bookingId", expression = "java(feedback.getBooking() != null ? feedback.getBooking().getBookingNumber() : null)")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "createdAt", source = "createAt")
    @Mapping(target = "reviewerName", source = "booking.account.profile.fullName")

    // Vehicle details
    @Mapping(target = "pickUpTime", source = "booking.pickUpTime")
    @Mapping(target = "dropOffTime", source = "booking.dropOffTime")
    @Mapping(target = "brand", source = "booking.car.brand")
    @Mapping(target = "model", source = "booking.car.model")
    @Mapping(target = "carImageFrontUrl", source = "booking.car.carImageFront")
    FeedbackDetailResponse toFeedbackDetailResponse(Feedback feedback);

    /**
     * Converts a list of Feedback entities to a list of FeedbackDetailResponse DTOs.
     *
     * @param feedbackList The list of Feedback entities.
     * @return The mapped list of FeedbackDetailResponse DTOs.
     */
    List<FeedbackDetailResponse> toFeedbackDetailResponseList(List<Feedback> feedbackList);
}
