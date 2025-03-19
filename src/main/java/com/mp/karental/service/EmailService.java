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

    //REGISTER
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
                + "<p><a href=\"" + confirmUrl + "\" style=\"color: blue; font-weight: bold;\">Verify Email</a></p>"
                + "<p>If you did not sign up for this service, please ignore this email.</p>";
        sendEmail(to, subject, htmlContent);
    }

    //FORGOT PASSWORD
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

    //RENT CAR
    /**
     * Sends an email to the customer notifying them that their booking is awaiting confirmation.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @param bookingNumber Booking reference number.
     * @throws MessagingException If email sending fails.
     */
    public void sendBookingWaitingForConfirmationEmail(String to, String carName, String bookingNumber) throws MessagingException {
        // Email content
        String subject = "Your Booking is Pending Confirmation";
        String htmlContent = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>Your booking for the car <strong>%s</strong> has been successfully placed and is now <strong>waiting for the car owner’s confirmation</strong>.</p>"
                        + "<p>You can check the status of your booking in the system with booking number <strong>%s</strong>.</p>"
                        + "<p>Thank you!</p>",
                carName, bookingNumber);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email to the car owner requesting confirmation for a booking.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @param licensePlate License plate of the car.
     * @throws MessagingException If email sending fails.
     */
    public void sendCarOwnerConfirmationRequestEmail(String to, String carName, String licensePlate) throws MessagingException {
        // Email content
        String subject = "A New Booking Needs Your Confirmation";
        String htmlContent = String.format(
                "<p>Dear Car Owner,</p>"
                        + "<p>Your car <strong>%s</strong> has been booked by a customer.</p>"
                        + "<p>Please check your account and confirm the booking with the license plate: <strong>%s</strong>.</p>"
                        + "<p>If you need assistance, please contact support.</p>"
                        + "<p>Thank you!</p>",
                carName, licensePlate);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email to notify the customer that their booking has been canceled by the system.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @param reason Reason for cancellation.
     * @throws MessagingException If email sending fails.
     */
    public void sendSystemCanceledBookingEmail(String to, String carName, String reason) throws MessagingException {
        String subject = "Your Booking Has Been Cancelled";
        String htmlContent = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>Your booking for the car <strong>%s</strong> has been <strong>cancelled</strong> due to the following reason: <strong>%s</strong>.</p>"
                        + "<p>If you have any questions, please contact our support team.</p>"
                        + "<p>Thank you!</p>",
                carName, reason);

        sendEmail(to, subject, htmlContent);
    }

    //CANCEL BOOKING
    /**
     * Sends an email to notify the customer that their booking has been successfully canceled.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @throws MessagingException If email sending fails.
     */
    public void sendCustomerBookingCanceledEmail(String to, String carName) throws MessagingException {
        String subject = "Booking Cancellation Successful";
        String htmlContent = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>Your booking for <strong>%s</strong> has been successfully canceled.</p>"
                        + "<p>If you have any questions, please contact our support team.</p>"
                        + "<p>Thank you!</p>",
                carName);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email to notify the customer that their booking has been canceled with a full refund.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @throws MessagingException If email sending fails.
     */
    public void sendCustomerBookingCanceledWithFullRefundEmail(String to, String carName) throws MessagingException {
        String subject = "Booking Cancellation Successful - Full Refund";
        String htmlContent = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>Your booking for <strong>%s</strong> has been successfully canceled.</p>"
                        + "<p>Your deposit has been fully refunded.</p>"
                        + "<p>If you have any questions, please contact our support team.</p>"
                        + "<p>Thank you!</p>",
                carName);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email to notify the customer that their booking has been canceled with a 70% refund.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @throws MessagingException If email sending fails.
     */
    public void sendCustomerBookingCanceledWithPartialRefundEmail(String to, String carName) throws MessagingException {
        String subject = "Booking Cancellation Successful - 70% Refund";
        String htmlContent = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>Your booking for <strong>%s</strong> has been successfully canceled.</p>"
                        + "<p>You have received a 70%% refund of your deposit.</p>"
                        + "<p>If you have any questions, please contact our support team.</p>"
                        + "<p>Thank you!</p>",
                carName);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an email to notify the car owner that a booking has been canceled.
     * @param to Recipient email address.
     * @param carName Name of the booked car.
     * @throws MessagingException If email sending fails.
     */
    public void sendCarOwnerBookingCanceledEmail(String to, String carName) throws MessagingException {
        String subject = "Booking Cancellation Notification";
        String htmlContent = String.format(
                "<p>Dear Car Owner,</p>"
                        + "<p>The booking for your car <strong>%s</strong> has been canceled by the customer.</p>"
                        + "<p>The system has retained 30%% of the deposit, and you will receive 22%% of it shortly.</p>"
                        + "<p>Thank you!</p>",
                carName);

        sendEmail(to, subject, htmlContent);
    }

    //RETURN CAR
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

    //APPROVE RENTAL
    /**
     * Sends a confirmation email to the customer regarding their car booking pickup.
     *
     * @param to            The recipient's email address (customer).
     * @param carName       The name of the booked car.
     * @param bookingNumber The unique booking number.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendConfirmPickUpEmail(String to, String carName, String bookingNumber) throws MessagingException {
        // Email subject including the booking number
        String subject = "Booking Pickup Confirmation - " + bookingNumber;

        // Email body with booking details and a request for the customer to confirm the pickup
        String body = "Dear Customer,\n\n"
                + "Your booking (Booking No: " + bookingNumber + ") for the car " + carName + " has been confirmed.\n"
                + "When you pick up the car, please confirm it in the system.\n\n"
                + "Thank you for choosing our service!\n"
                + "Best regards,\n"
                + "Your Car Rental Team";

        // Sending the email
        sendEmail(to, subject, body);
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
