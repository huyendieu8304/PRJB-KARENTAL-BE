package com.mp.karental.listener;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Car;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is a class used to test RedisPendingDepositExpiredListener, contains method to listen on redis key-value expire event
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class RedisPendingDepositExpiredListenerTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RedisPendingDepositExpiredListener listener;

    @Test
    void onMessage_ShouldCancelBooking_WhenBookingIsPendingDeposit() {
        // Arrange
        String bookingId = "12345";
        String key = "booking:" + bookingId;
        Message message = mock(Message.class);

        Booking booking = new Booking();
        booking.setBookingNumber(bookingId);
        booking.setStatus(EBookingStatus.PENDING_DEPOSIT);

        Account account = new Account();
        account.setEmail("test@example.com");
        booking.setAccount(account);

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        booking.setCar(car);

        when(message.getBody()).thenReturn(key.getBytes());
        when(bookingRepository.findByBookingNumber(bookingId)).thenReturn(Optional.of(booking));

        // Act
        listener.onMessage(message, null);

        // Assert
        verify(bookingRepository).findByBookingNumber(bookingId);
        verify(bookingRepository).save(booking);
        verify(emailService).sendCancelledBookingEmail(
                eq("test@example.com"),
                eq("Toyota Camry"),
                eq("Your booking was automatically canceled because the deposit was not paid within 1 hour.")
        );
        assert booking.getStatus() == EBookingStatus.CANCELLED;
    }

    @Test
    void onMessage_ShouldNotFindBooking_WhenSomethingElseExpired() {
        //Arrange
        String key = "token:12345";
        Message message = mock(Message.class);

        //mock
        when(message.getBody()).thenReturn(key.getBytes());

        //act
        listener.onMessage(message, null);
        // Assert
        verify(bookingRepository, never()).findByBookingNumber(anyString());
        verify(bookingRepository, never()).save(any());
        verify(emailService, never()).sendCancelledBookingEmail(any(), any(), any());

    }

    @Test
    void onMessage_ShouldNotCancelBooking_WhenBookingIsNotPendingDeposit() {
        // Arrange
        String bookingId = "12345";
        String key = "booking:" + bookingId;
        Message message = mock(Message.class);

        Booking booking = new Booking();
        booking.setBookingNumber(bookingId);
        booking.setStatus(EBookingStatus.CONFIRMED); // Không phải PENDING_DEPOSIT

        when(message.getBody()).thenReturn(key.getBytes());
        when(bookingRepository.findByBookingNumber(bookingId)).thenReturn(Optional.of(booking));

        // Act
        listener.onMessage(message, null);

        // Assert
        verify(bookingRepository).findByBookingNumber(bookingId);
        verify(bookingRepository, never()).save(booking);
        verify(emailService, never()).sendCancelledBookingEmail(any(), any(), any());
    }

    @Test
    void onMessage_ShouldNotCancelBooking_WhenBookingNotFound() {
        // Arrange
        String bookingId = "12345";
        String key = "booking:" + bookingId;
        Message message = mock(Message.class);

        when(message.getBody()).thenReturn(key.getBytes());
        when(bookingRepository.findByBookingNumber(bookingId)).thenReturn(Optional.empty());

        // Act
        listener.onMessage(message, null);

        // Assert
        verify(bookingRepository).findByBookingNumber(bookingId);
        verify(bookingRepository, never()).save(any());
        verify(emailService, never()).sendCancelledBookingEmail(any(), any(), any());
    }

}