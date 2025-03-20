package com.mp.karental.listener;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedisPendingDepositExpiredListener implements MessageListener {

    BookingRepository bookingRepository;
    EmailService emailService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = new String(message.getBody());
        if (key.startsWith("booking:")){
            String bookingId = key.split(":")[1];
            bookingRepository.findByBookingNumber(bookingId).ifPresent(booking -> {
                if (booking.getStatus().equals(EBookingStatus.PENDING_DEPOSIT)){
                    booking.setStatus(EBookingStatus.CANCELLED);
                    bookingRepository.save(booking);

                    String reason = "Your booking was automatically canceled because the deposit was not paid within 1 hour.";
                    try {
                        emailService.sendSystemCanceledBookingEmail(
                                booking.getAccount().getEmail(),
                                booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                                reason);
                    } catch (MessagingException e) {
                        throw new AppException(ErrorCode.SEND_SYSTEM_CANCEL_BOOKING_EMAIL_FAIL);
                    }

                    log.info("Booking with id " + bookingId + " has been cancelled due to expired of paying deposit time");
                }
            });
        }
    }
}
