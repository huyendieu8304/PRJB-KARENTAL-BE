package com.mp.karental.scheduler;
import static org.mockito.Mockito.*;

import com.mp.karental.service.BookingService;
import com.mp.karental.scheduler.BookingScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BookingSchedulerTest {

    @Mock
    private BookingService bookingService; // Mock BookingService

    @InjectMocks
    private BookingScheduler bookingScheduler; // Inject mock vào BookingScheduler

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckOverduePickUpAndDropOffBookings() {
        // When
        bookingScheduler.checkOverduePickUpAndDropOffBookings();

        // Then: Kiểm tra xem phương thức đã được gọi
        verify(bookingService, times(1)).processOverduePickUpAndDropOffBookings();
    }

    @Test
    void testCheckOverdueWaitingBookings() {
        // When
        bookingScheduler.checkOverdueWaitingBookings();

        // Then: Kiểm tra xem phương thức đã được gọi
        verify(bookingService, times(1)).processOverdueWaitingBookings();
    }
}

