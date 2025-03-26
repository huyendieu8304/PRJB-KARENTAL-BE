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

    @Scheduled(cron = "0 0 6,22 * * *") // check on 6h and 22h
    //@Scheduled(cron = "0 33 15 * * *") //test
    //@Scheduled(fixedDelay = 1000) // test
    public void checkOverdueBookings() {
        log.info("Checking overdue bookings...");
        bookingService.processOverdueBookings(); // both pick up time and drop off time
    }

    @Scheduled(cron = "0 0 6,22 * * *") // check on 6h and 22h
    public void checkOverdueWaitingBookings() {
        log.info("Checking overdue waiting bookings...");
        bookingService.processOverdueWaitingBookings();
    }
}
