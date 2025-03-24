package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.exception.AppException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * test email service
 *
 * QuangPM20
 * version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;
    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendBookingCancellationEmailToCarOwner_Failure() throws MessagingException {
        // Mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use doAnswer to throw the exception correctly
        doAnswer(invocation -> {
            throw new MessagingException("Failed to send email");
        }).when(mailSender).send(any(MimeMessage.class));

        // Verify that AppException is thrown
        assertThrows(AppException.class, () ->
                emailService.sendBookingCancellationEmailToCarOwner(
                        "customer@example.com",
                        "owner@example.com",
                        "Toyota Camry"
                )
        );

        // Verify send was attempted
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendBookingCancellationEmailToCustomer_Failure() throws MessagingException {
        // Mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use doAnswer to throw the exception correctly
        doAnswer(invocation -> {
            throw new MessagingException("Failed to send email");
        }).when(mailSender).send(any(MimeMessage.class));

        // Verify that AppException is thrown
        assertThrows(AppException.class, () ->
                emailService.sendBookingCancellationEmailToCustomer(
                        "customer@example.com",
                        EBookingStatus.PENDING_DEPOSIT,
                        "owner@example.com",
                        "Toyota Camry"
                )
        );

        // Verify send was attempted
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendCancelledEmail_Failure() throws MessagingException {
        // Mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use doAnswer to throw the exception correctly
        doAnswer(invocation -> {
            throw new MessagingException("Failed to send email");
        }).when(mailSender).send(any(MimeMessage.class));

        // Verify that AppException is thrown
        assertThrows(AppException.class, () ->
                emailService.sendCancelledBookingEmail(
                        "customer@example.com",
                        "owner@example.com",
                        "Toyota Camry"
                )
        );

        // Verify send was attempted
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWaitingConfirmedEmail_Failure() throws MessagingException {
        // Mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use doAnswer to throw the exception correctly
        doAnswer(invocation -> {
            throw new MessagingException("Failed to send email");
        }).when(mailSender).send(any(MimeMessage.class));

        // Verify that AppException is thrown
        assertThrows(AppException.class, () ->
                emailService.sendWaitingConfirmedEmail(
                        "customer@example.com",
                        "owner@example.com",
                        "Toyota Camry",
                        "12345"
                )
        );

        // Verify send was attempted
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRegisterEmail() throws MessagingException {
        emailService.sendRegisterEmail("test@example.com", "http://test.com/confirm");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendForgotPasswordEmail() throws MessagingException {
        emailService.sendForgotPasswordEmail("test@example.com", "http://test.com/reset");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWalletUpdateEmail() throws MessagingException {
        emailService.sendWalletUpdateEmail("test@example.com", "http://test.com/wallet");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWaitingConfirmedEmail() throws MessagingException {
        emailService.sendWaitingConfirmedEmail("customer@example.com", "owner@example.com", "Toyota Camry", "12345");
    }

    @Test
    void testSendCancelledBookingEmail() throws MessagingException {
        emailService.sendCancelledBookingEmail("customer@example.com", "Toyota Camry", "Owner unavailable");
    }

    @Test
    void testSendBookingCancellationEmailToCustomer() throws MessagingException {
        emailService.sendBookingCancellationEmailToCustomer("customer@example.com", EBookingStatus.CONFIRMED, "12345", "Toyota Camry");
    }
    @Test
    void testSendBookingCancellationEmailToCustomer1() throws MessagingException {
        emailService.sendBookingCancellationEmailToCustomer("customer@example.com", EBookingStatus.WAITING_CONFIRMED, "12345", "Toyota Camry");
    }
    @Test
    void testSendBookingCancellationEmailToCustomer2() throws MessagingException {
        emailService.sendBookingCancellationEmailToCustomer("customer@example.com", EBookingStatus.PENDING_DEPOSIT, "12345", "Toyota Camry");
    }

    @Test
    void testSendBookingCancellationEmailToCarOwner() throws MessagingException {
        emailService.sendBookingCancellationEmailToCarOwner("owner@example.com", "12345", "Toyota Camry");
    }

    @Test
    void testSendConfirmBookingEmail() throws MessagingException {
        emailService.sendConfirmBookingEmail("customer@example.com", "Toyota Camry", "12345");
    }

    @Test
    void testSendPaymentEmailToCustomer_Deduction() throws MessagingException {
        emailService.sendPaymentEmailToCustomer("customer@example.com", "12345", 100, false);
    }

    @Test
    void testSendPaymentEmailToCustomer_Refund() throws MessagingException {
        emailService.sendPaymentEmailToCustomer("customer@example.com", "12345", 50, true);

    }

    @Test
    void testSendPaymentEmailToCarOwner() throws MessagingException {
        emailService.sendPaymentEmailToCarOwner("owner@example.com", "12345", 200);
    }

    @Test
    void testSendPendingPaymentEmail() throws MessagingException {
        emailService.sendPendingPaymentEmail("customer@example.com", "12345", 75);
    }
}

