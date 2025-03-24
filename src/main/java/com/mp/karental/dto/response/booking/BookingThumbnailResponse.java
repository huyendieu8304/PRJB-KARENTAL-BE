package com.mp.karental.dto.response.booking;

import com.mp.karental.constant.EBookingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BookingThumbnailResponse {

    LocalDateTime updatedAt;

    String bookingNumber;

    EBookingStatus status;

    LocalDateTime pickUpTime;

    LocalDateTime dropOffTime;

    //(dropOffTime - pickupTime)%24
    int numberOfDay;

    long basePrice;

    //basePrice * numberOfDay
    long totalPrice;

    long deposit;

    //join car table
    String brand;

    String model;

    //sort desc
    int productionYear;

    //car image
    String carImageFrontUrl;
    String carImageBackUrl;
    String carImageLeftUrl;
    String carImageRightUrl;


}
