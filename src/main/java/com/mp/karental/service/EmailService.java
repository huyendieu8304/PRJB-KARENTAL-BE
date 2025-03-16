package com.mp.karental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service class for handling email notifications related to user registration, password reset,
 * car rental, booking cancellation, car return, and wallet updates.
 *
 * @author QuangPM20
 * @version 1.0
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;

//    @Value("${spring.mail.username}")
    private static String fromEmail = "childrencaresystemse1874@gmail.com"; // replace with your email

    /**
     * Sends a registration confirmation email.
     * @param to Recipient's email address.
     * @param confirmUrl URL for email verification.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendRegisterEmail(String to, String confirmUrl) throws MessagingException {
        String subject = "Welcome to Karental, " + to;
        String htmlContent = "<p><strong>Thank you for registering to our system!</strong></p>"
                + "<p>To continue using our services, please verify your email by clicking the link below:</p>"
                + "<p><a class= \"verify-email\" href=\"" + confirmUrl + "\" style=\"color: blue; font-weight: bold;\">Verify Email</a></p>"
                + "<p>If you did not sign up for this service, please ignore this email.</p>";
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends a password reset email.
     * @param to Recipient's email address.
     * @param forgotPasswordUrl URL for password reset.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendForgotPasswordEmail(String to, String forgotPasswordUrl) throws MessagingException {
        String subject = "Rent-a-car Password Reset";
        String htmlContent = "<p>We have just received a password reset request for " + to + ".</p>"
                + "<p>Please click <a href=\"" + forgotPasswordUrl + "\">here</a> to reset your password.</p>"
                + "<p>For your security, the link will expire in 24 hours or immediately after you reset your password.</p>";
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email confirmation for a successful car booking.
     * @param to Recipient's email address.
     * @param carName Name of the booked car.
     * @param walletUrl URL to check the wallet deposit.
     * @param carDetailsUrl URL to view car details.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendRentCarEmail(String to, String carName, String walletUrl, String carDetailsUrl) throws MessagingException {
        String subject = "Your car has been booked";
        String htmlContent = String.format(
                "<p>Congratulations! Your car <strong>%s</strong> has been booked at <strong>%s</strong>.</p>"
                        + "<p>Please go to your <a href=\"%s\">wallet</a> to check if the deposit has been paid "
                        + "and go to your <a href=\"%s\">car’s details page</a> to confirm the deposit.</p>"
                        + "<p>Thank you!</p>",
                carName, getCurrentFormattedDateTime(), walletUrl, carDetailsUrl);
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email notification when a car booking is canceled.
     * @param to Recipient's email address.
     * @param carName Name of the canceled car booking.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendCancelBookingEmail(String to, String carName) throws MessagingException {
        String subject = "A booking with your car has been cancelled";
        String htmlContent = String.format(
                "<p>Please be informed that a booking with your car <strong>%s</strong> has been cancelled at <strong>%s</strong>. "
                        + "The deposit will be returned to the customer’s wallet.</p>",
                carName, getCurrentFormattedDateTime());
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email notification when a rented car is returned.
     * @param to Recipient's email address.
     * @param carName Name of the returned car.
     * @param walletUrl URL to check the remaining payment.
     * @param carDetailsUrl URL to confirm payment details.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendCarReturnedEmail(String to, String carName, String walletUrl, String carDetailsUrl) throws MessagingException {
        String subject = "Your car has been returned";
        String htmlContent = String.format(
                "<p>Please be informed that your car <strong>%s</strong> has been returned at <strong>%s</strong>.</p>"
                        + "<p>Please go to your <a href=\"%s\">wallet</a> to check if the remaining payment has been paid "
                        + "and go to your <a href=\"%s\">car’s details page</a> to confirm the payment.</p>"
                        + "<p>Thank you!</p>",
                carName, getCurrentFormattedDateTime(), walletUrl, carDetailsUrl);
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email notification when the wallet balance is updated.
     * @param to Recipient's email address.
     * @param walletUrl URL to view the wallet balance and transactions.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendWalletUpdateEmail(String to, String walletUrl) throws MessagingException {
        String subject = "There’s an update to your wallet";
        String htmlContent = String.format(
                "<p>Please be informed that your wallet’s balance has been updated at <strong>%s</strong>.</p>"
                        + "<p>Please go to your <a href=\"%s\">wallet</a> and view the transactions for more details.</p>"
                        + "<p>Thank you!</p>",
                getCurrentFormattedDateTime(), walletUrl);
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Helper method to send an email with the given parameters.
     * @param to Recipient's email address.
     * @param subject Email subject.
     * @param htmlContent HTML-formatted email content.
     * @throws MessagingException If an error occurs while sending the email.
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setTo(to);
        helper.setFrom(fromEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    /**
     * Helper method to get the current date and time in a formatted string.
     * @return Formatted date-time string (dd/MM/yyyy HH:mm).
     */
    private String getCurrentFormattedDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
