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
            request.setPickUpTime(now.plusHours(3)); // Hợp lệ vì sau thời gian hiện tại 2 giờ
            request.setDropOffTime(now.plusHours(6)); // Hợp lệ vì sau thời gian lấy xe 4 giờ

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_PickUpInPast() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.minusHours(1)); // Không hợp lệ vì trong quá khứ
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
            request.setDropOffTime(now.plusHours(3)); // Không hợp lệ vì nhỏ hơn pick-up time

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
            request.setDropOffTime(now.plusDays(31)); // Không hợp lệ vì vượt quá 30 ngày

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
        // Mock thời gian hiện tại là 10:00 sáng
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(3)); // Hợp lệ: sau 3 giờ
            request.setDropOffTime(now.plusHours(6)); // Hợp lệ: sau pick-up 4 giờ

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }


    @Test
    void testInvalidBookingTime_Before6AM() {
        // Mock thời gian hiện tại là 3:00 sáng
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 3, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(2)); // Không hợp lệ: trước 06:00 sáng
            request.setDropOffTime(now.plusHours(6));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_Before8AMDropOff() {
        // Mock thời gian hiện tại là 5:00 sáng
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 5, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusHours(1)); // Hợp lệ: 06:00 sáng
            request.setDropOffTime(now.plusHours(2)); // Không hợp lệ: trước 08:00 sáng

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }



    @Test
    void testInvalidBookingTime_Exceeds60Days() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusDays(61)); // Không hợp lệ: vượt quá 60 ngày
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
            request.setPickUpTime(now.plusMinutes(90)); // Không hợp lệ: dưới 2 giờ
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
            request.setDropOffTime(now.plusHours(2)); // Không hợp lệ: dưới 4 giờ

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
            request.setDropOffTime(now.plusDays(30)); // Hợp lệ: đúng 30 ngày

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_PickUpAtMidnight() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 0, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusMinutes(30)); // Không hợp lệ: trước 06:00 sáng
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
            request.setPickUpTime(now.plusHours(2)); // Hợp lệ: đúng 06:00 sáng
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
            request.setDropOffTime(now.plusHours(5)); // Hợp lệ: đúng 08:00 sáng

            assertTrue(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testInvalidBookingTime_TooFarInFuture() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 10, 0);
        try (MockedStatic<LocalDateTime> mockedNow = Mockito.mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusYears(1)); // Không hợp lệ: xa hơn 60 ngày
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
            request.setPickUpTime(now.minusHours(1)); // Pick-up time trong quá khứ
            request.setDropOffTime(now.plusDays(1));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testDropOffBeforePickUp_ShouldBeInvalid() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 12, 0));
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 10, 0)); // Drop-off time trước pick-up

        assertFalse(bookingTimeValidator.isValid(request, context));
    }

    @Test
    void testRentalPeriodExceeds30Days_ShouldBeInvalid() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 12, 0));
        request.setDropOffTime(LocalDateTime.of(2024, 11, 15, 12, 0)); // Quá 30 ngày

        assertFalse(bookingTimeValidator.isValid(request, context));
    }


    @Test
    void testPickUpTooSoon_ShouldBeInvalid() {
        LocalDateTime now = LocalDateTime.of(2024, 10, 10, 12, 0);
        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(now);

            CreateBookingRequest request = new CreateBookingRequest();
            request.setPickUpTime(now.plusMinutes(30)); // Chỉ sau 30 phút, không đủ 2 giờ
            request.setDropOffTime(now.plusHours(5));

            assertFalse(bookingTimeValidator.isValid(request, context));
        }
    }

    @Test
    void testDropOffTooSoon_ShouldBeInvalid() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 12, 0));
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 13, 30)); // Chưa đủ 4 giờ

        assertFalse(bookingTimeValidator.isValid(request, context));
    }

    @Test
    void testPickUpBetween20hAnd4h_ShouldBeAfter6AM() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 10, 2, 0)); // Đặt vào lúc 2h sáng
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 10, 0));

        assertFalse(bookingTimeValidator.isValid(request, context));
    }

    @Test
    void testDropOffBetween20hAnd4h_ShouldBeAfter8AM() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(LocalDateTime.of(2024, 10, 9, 22, 0)); // Đặt xe lúc 22h
        request.setDropOffTime(LocalDateTime.of(2024, 10, 10, 6, 30)); // Drop-off trước 8h sáng

        assertFalse(bookingTimeValidator.isValid(request, context));
    }


    @Test
    void testPickUpTimeAfter20PM() {
        // Giả sử bây giờ là 21:00
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(21, 0));

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(now.plusDays(1).with(LocalTime.of(6, 0))); // 06:00 sáng ngày hôm sau
        request.setDropOffTime(now.plusDays(1).with(LocalTime.of(8, 0))); // 08:00 sáng ngày hôm sau

        assertTrue(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testInvalidPickUpTime6AM() {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(21, 0)); // 21:00

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(now.plusDays(1).with(LocalTime.of(6, 0))); // 05:00 sáng ngày hôm sau (không hợp lệ)
        request.setDropOffTime(now.plusDays(1).with(LocalTime.of(9, 0)));

        assertTrue(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testDropOff8AM() {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 0)); // 22:00

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(now.plusDays(1).with(LocalTime.of(6, 0))); // Hợp lệ
        request.setDropOffTime(now.plusDays(1).with(LocalTime.of(8, 0))); // Không hợp lệ (trước 08:00)

        assertTrue(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testPickUpBetween20And04() {
        // Giả lập thời gian hiện tại là 22:00 (trong khoảng 20:00 - 04:00)
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 0));
        LocalDateTime pickUp = now.plusDays(1).with(LocalTime.of(6, 30)); // Hợp lệ (sau 06:00)
        LocalDateTime dropOff = now.plusDays(1).with(LocalTime.of(8, 30)); // Hợp lệ (sau 08:00)

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertTrue(bookingTimeValidator.isValid(request, null));
    }
    @Test
    void testPickUpAfter60Days() {
        // Giả lập thời gian hiện tại là 21:00
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(21, 0));
        LocalDateTime pickUp = now.plusDays(61).with(LocalTime.of(6, 30)); // Không hợp lệ (> 60 ngày)
        LocalDateTime dropOff = now.plusDays(62).with(LocalTime.of(9, 0)); // Hợp lệ

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testDropOffAfter30Days() {
        // Giả lập thời gian hiện tại là 22:30
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(22, 30));
        LocalDateTime pickUp = now.plusDays(1).with(LocalTime.of(6, 30)); // Hợp lệ
        LocalDateTime dropOff = now.plusDays(31).with(LocalTime.of(9, 0)); // Không hợp lệ (> 30 ngày)

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testValidPickUpAndDropOff() {
        // Giả lập thời gian hiện tại là 23:45
        LocalDateTime now = LocalDateTime.now().with(LocalTime.of(23, 45));
        LocalDateTime pickUp = now.plusDays(1).with(LocalTime.of(7, 0)); // Hợp lệ (sau 06:00)
        LocalDateTime dropOff = now.plusDays(2).with(LocalTime.of(9, 0)); // Hợp lệ (sau 08:00)

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        assertTrue(bookingTimeValidator.isValid(request, null));
    }
    @Test
    void testSixAMAndEightAMAssignment() {
        // Giả lập thời gian hiện tại là 22:00
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

        // Kiểm tra xem sixAM và eightAM có được cập nhật đúng không
        assertEquals(today.plusDays(1).atTime(6, 0), sixAM);
        assertEquals(today.plusDays(1).atTime(8, 0), eightAM);
    }


    @Test
    void testNullPickUpOrDropOff() {
        CreateBookingRequest request = new CreateBookingRequest();

        // Trường hợp pickUpTime null
        request.setPickUpTime(null);
        request.setDropOffTime(LocalDateTime.now().plusDays(5));
        assertFalse(bookingTimeValidator.isValid(request, null));

        // Trường hợp dropOffTime null
        request.setPickUpTime(LocalDateTime.now().plusDays(5));
        request.setDropOffTime(null);
        assertFalse(bookingTimeValidator.isValid(request, null));

        // Cả hai đều null
        request.setPickUpTime(null);
        request.setDropOffTime(null);
        assertFalse(bookingTimeValidator.isValid(request, null));
    }

    @Test
    void testDaysBetweenExceeds30() {
        LocalDateTime pickUp = LocalDateTime.of(2024, 3, 10, 10, 0);
        LocalDateTime dropOffInvalid = pickUp.plusDays(31); // > 30 ngày

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOffInvalid);

        assertFalse(bookingTimeValidator.isValid(request, null)); // Phải return false
    }

    @Test
    void testPickUpAfter8PM() {
        // Giả lập thời gian hiện tại là 22:00 (sau 20:00)
        LocalDate today = LocalDate.of(2024, 3, 10);
        LocalDateTime now = today.atTime(22, 0);

        // Pick-up trước 6h sáng hôm sau → Không hợp lệ
        LocalDateTime pickUpInvalid = today.plusDays(1).atTime(5, 30);
        LocalDateTime dropOffValid = today.plusDays(1).atTime(9, 0);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUpInvalid);
        request.setDropOffTime(dropOffValid);

        assertFalse(bookingTimeValidator.isValid(request, null)); // Phải return false
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
        LocalTime nowTime = LocalTime.of(21, 0); // Giả lập đặt xe sau 20:00

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
        LocalTime nowTime = LocalTime.of(19, 0); // Giả lập đặt xe trước 20:00

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

        LocalDateTime now = today.atTime(19, 30); // Đặt xe lúc 19:30
        LocalDateTime pickUp = today.atTime(5, 59); // Pick-up trước 06:00 AM (không hợp lệ)
        LocalDateTime dropOff = today.atTime(8, 30); // Drop-off hợp lệ

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

        LocalDateTime now = today.atTime(22, 30); // Đặt xe lúc 22:30
        LocalDateTime pickUp = today.atTime(5, 59); // Pick-up trước 06:00 AM hôm sau (không hợp lệ)
        LocalDateTime dropOff = today.atTime(8, 30); // Drop-off hợp lệ

        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);

        BookingTimeValidator validator = new BookingTimeValidator();
        boolean result = validator.isValid(request, null);

        assertFalse(result, "Pick-up trước 06:00 AM hôm sau không hợp lệ khi đặt xe sau 20:00.");
    }

    @Test
    void testDropOffBeforeEightAM_ShouldReturnFalse_After20PM() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0); // Đặt xe lúc 22:00
        LocalDateTime pickUp = today.atTime(6, 30); // Pick-up hợp lệ (06:30 AM)
        LocalDateTime dropOff = today.atTime(7, 59); // Drop-off trước 08:00 AM

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);

        assertFalse(result, "Drop-off trước 08:00 AM phải bị từ chối.");
    }

    @Test
    void testPickUpBeforeSixAM_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0); // Đặt xe lúc 22:00
        LocalDateTime pickUp = today.atTime(5, 59); // Pick-up trước 06:00 AM
        LocalDateTime dropOff = today.atTime(10, 0); // Drop-off hợp lệ

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); // ❌ Vì pick-up trước 6:00 AM
    }

    @Test
    void testDropOffBeforeEightAM_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.atTime(6, 30); // Pick-up hợp lệ
        LocalDateTime dropOff = today.atTime(7, 59); // Drop-off trước 8:00 AM

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); // ❌ Vì drop-off trước 8:00 AM
    }

    @Test
    void testPickUpAfter60Days_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(61).atTime(6, 30); // Pick-up sau 60 ngày
        LocalDateTime dropOff = today.plusDays(62).atTime(10, 0); // Drop-off hợp lệ

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); // ❌ Vì pick-up sau 60 ngày
    }

    @Test
    void testDropOffAfter30Days_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(1).atTime(6, 30); // Pick-up hợp lệ
        LocalDateTime dropOff = today.plusDays(31).atTime(10, 0); // Drop-off sau 30 ngày

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertFalse(result); // ❌ Vì drop-off sau 30 ngày
    }

    @Test
    void testValidPickUpAndDropOff_ShouldReturnTrue() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(1).atTime(6, 30); // Pick-up hợp lệ
        LocalDateTime dropOff = today.plusDays(1).atTime(10, 0); // Drop-off hợp lệ

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertTrue(result); // ✅ Hợp lệ
    }

    @Test
    void testPickUpAtExactlySixAM_ShouldReturnTrue() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = today.atTime(22, 0);
        LocalDateTime pickUp = today.plusDays(1).atTime(6, 0); // Pick-up đúng 6:00 AM
        LocalDateTime dropOff = today.plusDays(1).atTime(10, 0); // Drop-off hợp lệ

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPickUpTime(pickUp);
        request.setDropOffTime(dropOff);
        boolean result = bookingTimeValidator.isValid(request, null);
        assertTrue(result); // ✅ Hợp lệ
    }



}

