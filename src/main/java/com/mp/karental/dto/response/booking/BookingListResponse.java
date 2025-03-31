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
@Schema(description = "Response containing a list of bookings and related statistics.")
public class BookingListResponse {

    @Schema(description = "Total number of ongoing bookings (not COMPLETED or CANCELLED).", example = "5")
    private int totalOnGoingBookings;

    @Schema(description = "Total number of bookings waiting for confirmation.", example = "2")
    private int totalWaitingConfirmBooking;

    @Schema(description = "Paginated list of booking details.")
    private Page<BookingThumbnailResponse> bookings;
}

