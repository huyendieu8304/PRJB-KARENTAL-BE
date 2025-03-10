package com.mp.karental.validation.validator;
import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.validation.validator.BookingTimeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingTimeValidatorTest {

    private BookingTimeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new BookingTimeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testNullPickUpOrDropOffTime_ShouldFail() {
        BookingRequest request = new BookingRequest();
        request.setPickUpTime(null);
        request.setDropOffTime(LocalDateTime.now().plusDays(1));

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testPickUpInPast_ShouldFail() {
        BookingRequest request = new BookingRequest();
        request.setPickUpTime(LocalDateTime.now().minusHours(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(1));

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testDropOffBeforePickUp_ShouldFail() {
        BookingRequest request = new BookingRequest();
        request.setPickUpTime(LocalDateTime.now().plusDays(2));
        request.setDropOffTime(LocalDateTime.now().plusDays(1));

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testRentalPeriodExceeds30Days_ShouldFail() {
        BookingRequest request = new BookingRequest();
        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(LocalDateTime.now().plusDays(31));

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testPickUpWithinValidTimeFrame_ShouldPass() {
        BookingRequest request = new BookingRequest();
        request.setPickUpTime(LocalDateTime.now().plusHours(3));
        request.setDropOffTime(LocalDateTime.now().plusDays(2));

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void testPickUpBetween20And4_ShouldEnforceNextDay6AMRule() {
        BookingRequest request = new BookingRequest();
        LocalDateTime now = LocalDateTime.now();

        if (now.getHour() >= 20 || now.getHour() < 4) {
            request.setPickUpTime(now.plusHours(1));
            request.setDropOffTime(now.plusDays(1).withHour(10));
            assertFalse(validator.isValid(request, context));
        }
    }
}

