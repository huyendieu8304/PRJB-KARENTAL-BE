package com.mp.karental.dto.request.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request payload for booking.
 * <p>
 * This class encapsulates the necessary data required to cancel a booking,
 * including booking number.
 * </p>
 * @author QuangPM20
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CancelBookingRequest {
    String bookingNumber;
}
