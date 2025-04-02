package com.mp.karental.dto.response.booking;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;


/**
 * Represents the response payload for a booking.
 * <p>
 * This class encapsulates booking information that is returned to the client,
 * including car details.
 * </p>
 * @author AnhHP9
 *
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@Schema(name = "response.booking.BookingListResponse", description = "Response DTO containing a list of booking thumbnails and summary counts.")
public class BookingListResponse {
    //count all booking isn't COMPLETED or CANCELLED
    @Schema(example = "5", description = "Total number of bookings that are not COMPLETED or CANCELLED.")
    int totalOnGoingBookings;

    //count all booking is WAITING_CONFIRMED
    @Schema(example = "2", description = "Total number of bookings in WAITING_CONFIRMED status.")
    int totalWaitingConfirmBooking;

    @Schema(description = "Paginated list of booking thumbnails.", implementation = BookingThumbnailResponse.class)
    private Page<BookingThumbnailResponse> bookings;
}

