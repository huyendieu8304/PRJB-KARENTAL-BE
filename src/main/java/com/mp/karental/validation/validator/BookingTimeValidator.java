package com.mp.karental.validation.validator;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.validation.ValidBookingTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Validator class for ensuring valid booking times based on specific business rules.
 */
public class BookingTimeValidator implements ConstraintValidator<ValidBookingTime, BookingRequest> {

    @Override
    public boolean isValid(BookingRequest bookingRequest, ConstraintValidatorContext context) {
        if (bookingRequest.getPickUpTime() == null || bookingRequest.getDropOffTime() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        LocalDateTime pickUpDateTime = bookingRequest.getPickUpTime();
        LocalDateTime dropOffDateTime = bookingRequest.getDropOffTime();

        // Pick-up time cannot be in the past and must be before drop-off
        if (pickUpDateTime.isBefore(now) || pickUpDateTime.isAfter(dropOffDateTime)) {
            return false;
        }

        // Define boundaries
        LocalTime PICKUP_START_TIME = LocalTime.of(6, 0);
        LocalTime PICKUP_END_TIME = LocalTime.of(22, 0);
        LocalTime DROPOFF_START_TIME = LocalTime.of(6, 0);
        LocalTime DROPOFF_END_TIME = LocalTime.of(22, 0);

        // Check if pick-up time is within allowed range (06:00 - 22:00)
        LocalTime pickUpTime = pickUpDateTime.toLocalTime();
        if (pickUpTime.isBefore(PICKUP_START_TIME) || pickUpTime.isAfter(PICKUP_END_TIME)) {
            return false;
        }

        // Pick-up must be at least 2 hours after booking and within 60 days
        if (pickUpDateTime.isBefore(now.plusHours(2)) || pickUpDateTime.toLocalDate().isAfter(today.plusDays(60))) {
            return false;
        }

        // Drop-off must be at least 4 hours after booking and within 30 days
        if (dropOffDateTime.isBefore(now.plusHours(4)) || dropOffDateTime.toLocalDate().isAfter(today.plusDays(30))) {
            return false;
        }
        if(pickUpDateTime.isAfter(dropOffDateTime.minusHours(2))){
            return false;
        }
        // Drop-off must be after 08:00 AM
        LocalTime dropOffTime = dropOffDateTime.toLocalTime();
        if (dropOffTime.isBefore(DROPOFF_START_TIME) || dropOffTime.isAfter(DROPOFF_END_TIME)) {
            return false;
        }

        return true;
    }
}