package com.mp.karental.scheduler;

import com.mp.karental.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingScheduler {
    BookingService bookingService;

    /**
     * check at 6h to 22h every day, if a booking have status waiting confirm
     * or waiting confirm return car will update by method processOverdueWaitingBookings
     * check every hour
     */
    @Scheduled(cron = "0 0 6-22 * * *")
    //@Scheduled(cron = "0 33 15 * * *") //test 15:33
    //@Scheduled(fixedDelay = 1000) // auto after 1 second
    public void checkOverduePickUpAndDropOffBookings() {
        log.info("Checking overdue pick up and drop off bookings...");
        bookingService.processOverduePickUpAndDropOffBookings(); // both pick up time and drop off time
    }

    /**
     * check at 6h05 to 22h05 every day, if a booking have status waiting confirm
     * or waiting confirm return car will update by method processOverdueWaitingBookings
     * check every hour
     */
    @Scheduled(cron = "0 5 6-22 * * *")
    //@Scheduled(fixedDelay = 60000) // auto after 60 second
    public void checkOverdueWaitingBookings() {
        log.info("Checking overdue waiting bookings...");
        bookingService.processOverdueWaitingBookings();
    }
}
