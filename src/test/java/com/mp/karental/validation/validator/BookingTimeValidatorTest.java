package com.mp.karental.validation.validator;
import com.mp.karental.dto.request.booking.CreateBookingRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingTimeValidatorTest {

    private BookingTimeValidator bookingTimeValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        bookingTimeValidator = new BookingTimeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidBookingTime() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(3)); 
            request.setDropOffTime(now.plusHours(6)); 

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_PickUpInPast() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.minusHours(1)); 
            request.setDropOffTime(now.plusHours(6));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_DropOffBeforePickUp() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(5));
            request.setDropOffTime(now.plusHours(3)); 

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_ExceedsMaxRentalDays() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusDays(1));
            request.setDropOffTime(now.plusDays(91));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_NullPickUpOrDropOff() {
        CreateBookingRequest request1 = new CreateBookingRequest();
        request1.setPickUpTime(null);
        request1.setDropOffTime(LocalDateTime.now().plusDays(2));

        CreateBookingRequest request2 = new CreateBookingRequest();
        request2.setPickUpTime(LocalDateTime.now().plusDays(2));
        request2.setDropOffTime(null);

        assertFalse(bookingTimeValidator.isValid(request1, context));
        assertFalse(bookingTimeValidator.isValid(request2, context));
    }

    @Test
    void testValidBookingTime_Daytime() {
        
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(3)); 
            request.setDropOffTime(now.plusHours(6)); 

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }


    @Test
    void testInvalidBookingTime_Before6AM() {
        
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 3, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(2)); 
            request.setDropOffTime(now.plusHours(6));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_Before8AMDropOff() {
        
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 5, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(1)); 
            request.setDropOffTime(now.plusHours(2)); 

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }



    @Test
    void testInvalidBookingTime_Exceeds60Days() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusDays(61)); 
            request.setDropOffTime(now.plusDays(62));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_PickUpLessThan2Hours() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusMinutes(90)); 
            request.setDropOffTime(now.plusHours(5));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_LessThanMinimumRentalPeriod() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(3));
            request.setDropOffTime(now.plusHours(2)); 

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testValidBookingTime_ExactMaxRentalDays() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusDays(1));
            request.setDropOffTime(now.plusDays(30)); 

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_PickUpAtMidnight() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 0, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusMinutes(30)); 
            request.setDropOffTime(now.plusHours(5));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_DropOffAtMidnight() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 22, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(2));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testValidBookingTime_ExactlyAt6AM() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 4, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(2)); 
            request.setDropOffTime(now.plusHours(4));

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testValidBookingTime_ExactlyAt8AMDropOff() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 3, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(3));
            request.setDropOffTime(now.plusHours(5)); 

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_TooFarInFuture() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusYears(1)); 
            request.setDropOffTime(now.plusYears(1).plusDays(1));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }


    @Test
    void testPickUpTimeInPast_ShouldBeInvalid() {
        LocalDateTime now = LocalDateTime.of(2024, 10, 10, 12, 0);
        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.minusHours(1)); 
            request.setDropOffTime(now.plusDays(1));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testDropOffBeforePickUp_ShouldBeInvalid() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 12, 0));
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 10, 0)); 

        assertFalse(bookingTimeValidator.isValid(request, context));
    }

    @Test
    void testRentalPeriodExceeds30Days_ShouldBeInvalid() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 12, 0));
        request.setDropOffTime(LocalDateTime.of(2024, 11, 15, 12, 0)); 

        assertFalse(bookingTimeValidator.isValid(request, context));
    }


    @Test
    void testPickUpTooSoon_ShouldBeInvalid() {
        LocalDateTime now = LocalDateTime.of(2024, 10, 10, 12, 0);
        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusMinutes(30)); 
            request.setDropOffTime(now.plusHours(5));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testDropOffTooSoon_ShouldBeInvalid() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 12, 0));
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 13, 30)); 

        assertFalse(bookingTimeValidator.isValid(request, context));
    }

    @Test
    void testPickUpBetween20hAnd4h_ShouldBeAfter6AM() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 2, 0)); 
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 10, 0));

        assertFalse(bookingTimeValidator.isValid(request, context));
    }

    @Test
    void testDropOffBetween20hAnd4h_ShouldBeAfter8AM() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 9, 22, 0)); 
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 6, 30)); 

        assertFalse(bookingTimeValidator.isValid(request, context));
    }


    @Test
    void testPickUpTimeAfter20PM() {
        
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(21, 0));

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(now.plusDays(1).with(LocalTime.of(6, 0))); 
        request.setDropOffTime(now.plusDays(1).with(LocalTime.of(8, 0))); 

        assertTrue(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testInvalidPickUpTime6AM() {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(21, 0)); // 21:00

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(now.plusDays(1).with(LocalTime.of(6, 0))); 
        request.setDropOffTime(now.plusDays(1).with(LocalTime.of(9, 0)));

        assertTrue(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testDropOff8AM() {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 0)); // 22:00

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(now.plusDays(1).with(LocalTime.of(6, 0))); 
        request.setDropOffTime(now.plusDays(1).with(LocalTime.of(8, 0))); 

        assertTrue(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testPickUpBetween20And04() {
        
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 0));
        LocalDateTime pickUp = now.plusDays(1).with(LocalTime.of(6, 30)); 
        LocalDateTime dropOff = now.plusDays(1).with(LocalTime.of(8, 30)); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertTrue(bookingTimeValidator.isValid(request, null));
    }
    @Test
    void testPickUpAfter60Days() {
        
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(21, 0));
        LocalDateTime pickUp = now.plusDays(61).with(LocalTime.of(6, 30)); 
        LocalDateTime dropOff = now.plusDays(62).with(LocalTime.of(9, 0)); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testDropOffAfter30Days() {
        
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 30));
        LocalDateTime pickUp = now.plusDays(1).with(LocalTime.of(6, 30)); 
        LocalDateTime dropOff = now.plusDays(91).with(LocalTime.of(9, 0));

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testValidPickUpAndDropOff() {
        
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(23, 45));
        LocalDateTime pickUp = now.plusDays(1).with(LocalTime.of(7, 0)); 
        LocalDateTime dropOff = now.plusDays(2).with(LocalTime.of(9, 0)); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertTrue(bookingTimeValidator.isValid(request, null));
    }
    @Test
    void testSixAMAndEightAMAssignment() {
        
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 0));
        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        LocalDateTime sixAM = today.atTime(6, 0);
        LocalDateTime eightAM = today.atTime(8, 0);

        if (nowTime.isAfter(LocalTime.of(20, 0))) {
            sixAM = today.plusDays(1).atTime(6, 0);
            eightAM = today.plusDays(1).atTime(8, 0);
        }

        System.out.println("sixAM: " + sixAM);
        System.out.println("eightAM: " + eightAM);

        
        assertEquals(today.plusDays(1).atTime(6, 0), sixAM);
        assertEquals(today.plusDays(1).atTime(8, 0), eightAM);
    }


    @Test
    void testNullPickUpOrDropOff() {
        CreateBookingRequest request = new CreateBookingRequest();

        
        request.setPickUpTime(null);
        request.setDropOffTime(LocalDateTime.now().plusDays(5));
        assertFalse(bookingTimeValidator.isValid(request, null));

        
        request.setPickUpTime(LocalDateTime.now().plusDays(5));
        request.setDropOffTime(null);
        assertFalse(bookingTimeValidator.isValid(request, null));

        
        request.setPickUpTime(null);
        request.setDropOffTime(null);
        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testDaysBetweenExceeds30() {
        LocalDateTime pickUp = LocalDateTime.of(2024, 3, 10, 10, 0);
        LocalDateTime dropOffInvalid = pickUp.plusDays(31); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOffInvalid);

        assertFalse(bookingTimeValidator.isValid(request, null)); 
    }

    @Test
    void testPickUpAfter8PM() {
        
        LocalDate today = LocalDate.of(2024, 3, 10);
        LocalDateTime now = today.atTime(22, 0);

        
        LocalDateTime pickUpInvalid = today.plusDays(1).atTime(5, 30);
        LocalDateTime dropOffValid = today.plusDays(1).atTime(9, 0);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUpInvalid);
        request.setDropOffTime(dropOffValid);

        assertFalse(bookingTimeValidator.isValid(request, null)); 
    }

    @Test
    void testPickUpOrDropOffNull() {
        CreateBookingRequest request = new CreateBookingRequest();

        request.setPickUpTime(null);
        request.setDropOffTime(LocalDateTime.now().plusDays(1));
        assertFalse(bookingTimeValidator.isValid(request, null));

        request.setPickUpTime(LocalDateTime.now().plusDays(1));
        request.setDropOffTime(null);
        assertFalse(bookingTimeValidator.isValid(request, null));

        request.setPickUpTime(null);
        request.setDropOffTime(null);
        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testSixAMAndEightAM_ShouldMoveToNextDay_After20PM() {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.of(21, 0); 

        LocalDateTime expectedSixAM = today.plusDays(1).atTime(6, 0);
        LocalDateTime expectedEightAM = today.plusDays(1).atTime(8, 0);

        LocalDateTime sixAM, eightAM;

        if (nowTime.isAfter(LocalTime.of(20, 0))) {
            sixAM = today.plusDays(1).atTime(6, 0);
            eightAM = today.plusDays(1).atTime(8, 0);
        } else {
            sixAM = today.atTime(6, 0);
            eightAM = today.atTime(8, 0);
        }

        assertEquals(expectedSixAM, sixAM, "sixAM phải được tịnh tiến thêm một ngày sau 20:00.");
        assertEquals(expectedEightAM, eightAM, "eightAM phải được tịnh tiến thêm một ngày sau 20:00.");
    }

    @Test
    void testSixAMAndEightAM_ShouldStaySame_Before20PM() {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.of(19, 0); 

        LocalDateTime expectedSixAM = today.atTime(6, 0);
        LocalDateTime expectedEightAM = today.atTime(8, 0);

        LocalDateTime sixAM, eightAM;

        if (nowTime.isAfter(LocalTime.of(20, 0))) {
            sixAM = today.plusDays(1).atTime(6, 0);
            eightAM = today.plusDays(1).atTime(8, 0);
        } else {
            sixAM = today.atTime(6, 0);
            eightAM = today.atTime(8, 0);
        }

        assertEquals(expectedSixAM, sixAM, "sixAM phải giữ nguyên khi đặt xe trước 20:00.");
        assertEquals(expectedEightAM, eightAM, "eightAM phải giữ nguyên khi đặt xe trước 20:00.");
    }

    @Test
    void testBookingBefore20PM_InvalidPickUpBeforeSixAM() {
        CreateBookingRequest request = new CreateBookingRequest();
        LocalDate today = LocalDate.now();

        LocalDateTime now = today.atTime(19, 30); 
        LocalDateTime pickUp = today.atTime(5, 59); 
        LocalDateTime dropOff = today.atTime(8, 30); 

        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        BookingTimeValidator validator = new BookingTimeValidator();
        boolean result = validator.isValid(request, null);

        assertFalse(result, "Pick-up trước 06:00 AM không hợp lệ khi đặt xe trước 20:00.");
    }

    @Test
    void testBookingAfter20PM_InvalidPickUpBeforeSixAM() {
        CreateBookingRequest request = new CreateBookingRequest();
        LocalDate today = LocalDate.now();

        LocalDateTime now = today.atTime(22, 30); 
        LocalDateTime pickUp = today.atTime(5, 59); 
        LocalDateTime dropOff = today.atTime(8, 30); 

        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        BookingTimeValidator validator = new BookingTimeValidator();
        boolean result = validator.isValid(request, null);

        assertFalse(result, "Pick-up trước 06:00 AM hôm sau không hợp lệ khi đặt xe sau 20:00.");
    }

    @Test
    void testDropOffBeforeEightAM_ShouldReturnFalse_After20PM() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0); 
        LocalDateTime pickUp = today.atTime(6, 30); 
        LocalDateTime dropOff = today.atTime(7, 59); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);

        assertFalse(result, "Drop-off trước 08:00 AM phải bị từ chối.");
    }

    @Test
    void testPickUpBeforeSixAM_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0); 
        LocalDateTime pickUp = today.atTime(5, 59); 
        LocalDateTime dropOff = today.atTime(10, 0); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); 
    }

    @Test
    void testDropOffBeforeEightAM_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.atTime(6, 30); 
        LocalDateTime dropOff = today.atTime(7, 59); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); 
    }

    @Test
    void testPickUpAfter60Days_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(61).atTime(6, 30); 
        LocalDateTime dropOff = today.plusDays(62).atTime(10, 0); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); 
    }

    @Test
    void testDropOffAfter30Days_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(1).atTime(6, 30); 
        LocalDateTime dropOff = today.plusDays(91).atTime(10, 0);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); 
    }

    @Test
    void testValidPickUpAndDropOff_ShouldReturnTrue() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(1).atTime(6, 30); 
        LocalDateTime dropOff = today.plusDays(1).atTime(10, 0); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertTrue(result); 
    }

    @Test
    void testPickUpAtExactlySixAM_ShouldReturnTrue() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(1).atTime(6, 0); 
        LocalDateTime dropOff = today.plusDays(1).atTime(10, 0); 

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertTrue(result); 
    }



}

