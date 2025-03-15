package com.mp.karental.dto.response;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
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
public class BookingListResponse {
    //count all booking isn't COMPLETED or CANCELLED
    int totalOnGoingBookings;

    //count all booking is WAITING_CONFIRMED
    int totalWaitingConfirmBooking;

    private Page<BookingThumbnailResponse> bookings;
}
