package com.mp.karental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

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

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
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
    void testSendCarReturnedEmail() throws MessagingException {
        emailService.sendCarReturnedEmail("test@example.com", "Toyota", "http://test.com/wallet", "http://test.com/car");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWalletUpdateEmail() throws MessagingException {
        emailService.sendWalletUpdateEmail("test@example.com", "http://test.com/wallet");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendBookingWaitingForConfirmationEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendBookingWaitingForConfirmationEmail("customer@example.com", "Toyota Camry", "B123");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCarOwnerConfirmationRequestEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendCarOwnerConfirmationRequestEmail("owner@example.com", "Toyota Camry", "ABC-123");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendSystemCanceledBookingEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendSystemCanceledBookingEmail("customer@example.com", "Toyota Camry", "Payment failure");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomerBookingCanceledEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendCustomerBookingCanceledEmail("customer@example.com", "Toyota Camry");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomerBookingCanceledWithFullRefundEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendCustomerBookingCanceledWithFullRefundEmail("customer@example.com", "Toyota Camry");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomerBookingCanceledWithPartialRefundEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendCustomerBookingCanceledWithPartialRefundEmail("customer@example.com", "Toyota Camry");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCarOwnerBookingCanceledEmail_ShouldSendEmail() throws MessagingException {
        emailService.sendCarOwnerBookingCanceledEmail("owner@example.com", "Toyota Camry");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}

