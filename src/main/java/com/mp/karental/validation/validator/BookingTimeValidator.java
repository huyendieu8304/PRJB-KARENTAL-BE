package com.mp.karental.validation.validator;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.validation.ValidBookingTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Validator class for ensuring valid booking times based on specific business rules.
 */
public class BookingTimeValidator implements ConstraintValidator<ValidBookingTime, BookingRequest> {

    @Override
    public boolean isValid(BookingRequest bookingRequest, ConstraintValidatorContext context) {
        // Ensure that both pick-up and drop-off times are provided
        if (bookingRequest.getPickUpTime() == null || bookingRequest.getDropOffTime() == null) {
            return false;
        }

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pickUpDateTime = bookingRequest.getPickUpTime();
        LocalDateTime dropOffDateTime = bookingRequest.getDropOffTime();

        // Calculate the number of days between pick-up and drop-off dates
        long daysBetween = ChronoUnit.DAYS.between(pickUpDateTime.toLocalDate(), dropOffDateTime.toLocalDate());

        // Reject if the pick-up time is in the past
        if (pickUpDateTime.isBefore(now)) {
            return false;
        }
        if(pickUpDateTime.isAfter(dropOffDateTime)) {
            return false;
        }

        // Reject if the rental period exceeds 30 days
        if (daysBetween > 30) {
            return false;
        }

        // Extract the current time and date for further validation
        LocalTime nowTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();

        LocalDateTime sixAM = now.withHour(6).withMinute(0);
        LocalDateTime eightAM = now.withHour(8).withMinute(0);

        // Validate pick-up and drop-off time based on the time of the day
        if (nowTime.isAfter(LocalTime.of(4, 0)) && nowTime.isBefore(LocalTime.of(20, 0))) {
            // Between 04:00 and 20:00, pick-up must be at least 2 hours later, and within 60 days
            // Drop-off must be at least 4 hours after booking

            //another condition is like 20h-4h
            boolean isValidPickUpNextDay = pickUpDateTime.toLocalDate().equals(today) || !pickUpDateTime.isBefore(sixAM);
            boolean isValidDropOffNextDay = dropOffDateTime.toLocalDate().equals(today) || !dropOffDateTime.isBefore(eightAM);

            return !(pickUpDateTime.isBefore(now.plusHours(2)) || pickUpDateTime.toLocalDate().isAfter(today.plusDays(60)))
                    && !(dropOffDateTime.isBefore(now.plusHours(4)) || dropOffDateTime.toLocalDate().isAfter(today.plusDays(30)))
                    && isValidDropOffNextDay && isValidPickUpNextDay;
        } else {
            // Between 20:00 and 04:00, pick-up must be at least 06:00 AM, and within 60 days
            // Drop-off must be at least 08:00 AM after booking

            // 20:00-23:59
            if (nowTime.isAfter(LocalTime.of(20, 0))) {
                sixAM = sixAM.plusDays(1);
                eightAM = eightAM.plusDays(1);
            }

            boolean isValidPickUpNextDay = pickUpDateTime.toLocalDate().isEqual(today) || !pickUpDateTime.isBefore(pickUpDateTime.toLocalDate().atTime(6, 0));
            boolean isValidDropOffNextDay = dropOffDateTime.toLocalDate().isEqual(today) || !dropOffDateTime.isBefore(dropOffDateTime.toLocalDate().atTime(8, 0));

            return !(pickUpDateTime.isBefore(sixAM) || pickUpDateTime.toLocalDate().isAfter(today.plusDays(60)))
                    && !(dropOffDateTime.isBefore(eightAM) || dropOffDateTime.toLocalDate().isAfter(today.plusDays(30)))
                    && isValidPickUpNextDay && isValidDropOffNextDay;
        }
    }

}
