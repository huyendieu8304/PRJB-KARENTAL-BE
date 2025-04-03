package com.mp.karental.listener;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.ERole;
import com.mp.karental.constant.ETransactionStatus;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.TransactionRepository;
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
    TransactionRepository transactionRepository;

    private static final String PROCESSING_TRANSACTION_PREFIX = "trans:";
    private static final String PENDING_DEPOSIT_BOOKING_KEY = "booking:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = new String(message.getBody());
        if (key.startsWith(PENDING_DEPOSIT_BOOKING_KEY)) {
            String bookingId = key.split(":")[1];
            bookingRepository.findByBookingNumber(bookingId).ifPresent(booking -> {
                if (booking.getStatus().equals(EBookingStatus.PENDING_DEPOSIT)){
                    booking.setStatus(EBookingStatus.CANCELLED);
                    log.info("Booking: {} has been cancelled due to expired of paying deposit time", bookingId);
                    bookingRepository.save(booking);

                    String reason = "Your booking was automatically canceled because the deposit was not paid within 1 hour.";
                    emailService.sendCancelledBookingEmail(booking.getAccount().getEmail(),
                            booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                            reason);

                }
            });
        }
        if (key.startsWith(PROCESSING_TRANSACTION_PREFIX)) {
            String transactionId = key.split(":")[1];
            transactionRepository.findById(transactionId).ifPresent(transaction -> {
                if (transaction.getStatus().equals(ETransactionStatus.PROCESSING)){
                    transaction.setStatus(ETransactionStatus.FAILED);
                    log.info("Transaction: {} has been cancelled due to expired of paying transaction time.",  transactionId);
                    transactionRepository.save(transaction);
                }
            });
        }
    }
}
