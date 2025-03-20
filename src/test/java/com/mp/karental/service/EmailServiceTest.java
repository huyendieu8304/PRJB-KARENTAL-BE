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

//    @Test
//    void testSendRentCarEmail() throws MessagingException {
//        emailService.sendRentCarEmail("test@example.com", "Toyota", "http://test.com/wallet", "http://test.com/car");
//        verify(mailSender, times(1)).send(any(MimeMessage.class));
//    }
//
//    @Test
//    void testSendCancelBookingEmail() throws MessagingException {
//        emailService.sendCancelBookingEmail("test@example.com", "Toyota");
//        verify(mailSender, times(1)).send(any(MimeMessage.class));
//    }

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
}

