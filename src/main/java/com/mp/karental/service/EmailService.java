package com.mp.karental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendRegisterEmail(String to, String confirmUrl) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // Nội dung email có HTML
        String htmlContent = "<p><strong>Thank you for registering to our system!</strong></p>"
                + "<p>To continue using our services, please verify your email by clicking the link below:</p>"
                + "<p><a href=\"" + confirmUrl + "\" style=\"color: blue; font-weight: bold;\">Verify Email</a></p>"
                + "<p>If you did not sign up for this service, please ignore this email.</p>";

        helper.setTo(to);
        helper.setFrom("your_email@gmail.com"); // Đổi thành email của bạn
        helper.setSubject("Welcome to Karental, " + to);
        helper.setText(htmlContent, true); // 'true' để kích hoạt HTML

        mailSender.send(mimeMessage);
    }

    public void sendForgotPasswordEmail(String to, String forgotPasswordUrl) throws MessagingException {
        // Create a MimeMessage
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // Use a helper to set subject, from, to, etc.
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // HTML body with an <a> tag
        String htmlMsg = "<p>We have just received a password reset request for " + to + ".</p>"
                + "<p>Please click <a href=\"" + forgotPasswordUrl + "\">here</a> to reset your password.</p>"
                + "<p>For your security, the link will expire in 24 hours or immediately after you reset your password.</p>";

        // Set the MIME type to text/html
        helper.setText(htmlMsg, true);
        helper.setTo(to);
        helper.setSubject("Rent-a-car Password Reset");

        // Send the message
        mailSender.send(mimeMessage);
    }

    public void sendRentCarEmail(String to, String carName, String walletUrl, String carDetailsUrl) throws MessagingException{
        // Get current date and time
        LocalDateTime bookingDateTime = LocalDateTime.now();

        // Format date-time (e.g., "25/10/2024 14:30")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDateTime = bookingDateTime.format(formatter);

        // Create a MIME message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // Build email content with clickable links
        String htmlMsg = "<p>Congratulations! Your car <strong>" + carName + "</strong> "
                + "has been booked at <strong>" + formattedDateTime + "</strong>.</p>"
                + "<p>Please go to your <a href=\"" + walletUrl + "\">wallet</a> to check if the deposit has been paid "
                + "and go to your <a href=\"" + carDetailsUrl + "\">car’s details page</a> to confirm the deposit.</p>"
                + "<p>Thank you!</p>";

        // Set email properties
        helper.setText(htmlMsg, true);
        helper.setSubject("Your car has been booked");
        helper.setFrom("your_email@gmail.com"); // Đổi thành email của bạn
        helper.setTo(to);

        // Send the email
        mailSender.send(mimeMessage);
    }

    public void sendCancelBookingEmail(String to, String carName) throws MessagingException {
        // Get the current date and time
        LocalDateTime cancellationDateTime = LocalDateTime.now();

        // Format date and time (e.g., "25/10/2024 14:30")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDateTime = cancellationDateTime.format(formatter);

        // Create a MIME message (necessary for HTML content)
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // Build the email body
        String htmlMsg = "<p>Please be informed that a booking with your car <strong>"
                + carName + "</strong> has been cancelled at <strong>"
                + formattedDateTime + "</strong>. The deposit will be returned to the customer’s wallet.</p>";

        // Set email properties
        helper.setText(htmlMsg, true);
        helper.setSubject("A booking with your car has been cancelled");
        helper.setFrom("your_email@gmail.com"); // Đổi thành email của bạn
        helper.setTo(to);

        // Send the email
        mailSender.send(mimeMessage);
    }

    public void sendCarReturnedEmail(String to, String carName, String walletUrl, String carDetailsUrl)
            throws MessagingException {
        // Get current date and time
        LocalDateTime returnDateTime = LocalDateTime.now();

        // Format date-time (e.g., "25/10/2024 14:30")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDateTime = returnDateTime.format(formatter);

        // Create a MIME message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // Build email content with clickable links
        String htmlMsg = "<p>Please be informed that your car <strong>" + carName + "</strong> "
                + "has been returned at <strong>" + formattedDateTime + "</strong>.</p>"
                + "<p>Please go to your <a href=\"" + walletUrl + "\">wallet</a> to check if the remaining payment has been paid "
                + "and go to your <a href=\"" + carDetailsUrl + "\">car’s details page</a> to confirm the payment.</p>"
                + "<p>Thank you!</p>";

        // Set email properties
        helper.setText(htmlMsg, true);
        helper.setFrom("your_email@gmail.com"); // Đổi thành email của bạn
        helper.setSubject("Your car has been returned");
        helper.setTo(to);

        // Send the email
        mailSender.send(mimeMessage);
    }

    public void sendWalletUpdateEmail(String to, String walletUrl) throws MessagingException {
        // Get current date and time
        LocalDateTime updateDateTime = LocalDateTime.now();

        // Format date-time (e.g., "25/10/2024 14:30")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDateTime = updateDateTime.format(formatter);

        // Create a MIME message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // Build email content with a clickable link
        String htmlMsg = "<p>Please be informed that your wallet’s balance has been updated at <strong>"
                + formattedDateTime + "</strong>.</p>"
                + "<p>Please go to your <a href=\"" + walletUrl + "\">wallet</a> and view the transactions for more details.</p>"
                + "<p>Thank you!</p>";

        // Set email properties
        helper.setText(htmlMsg, true);
        helper.setSubject("There’s an update to your wallet");
        helper.setFrom("your_email@gmail.com"); // Đổi thành email của bạn
        helper.setTo(to);

        // Send the email
        mailSender.send(mimeMessage);
    }
}

