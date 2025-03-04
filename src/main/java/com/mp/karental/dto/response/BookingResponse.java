package com.mp.karental.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO representing the detailed information of a booking.
 * This class is used to transfer booking data from the backend to the client.
 * It encapsulates all necessary booking details in a structured format.
 * @author AnhHP9
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BookingResponse {
}
