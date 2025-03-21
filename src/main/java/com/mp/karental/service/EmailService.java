package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.ERole;
import com.mp.karental.entity.Account;
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
    public void sendBookingEmail(EBookingStatus status, ERole role, String to, String carName, String detail) throws MessagingException {
        String subject = "", body = "";

        switch (status) {
            case WAITING_CONFIRMED:
                if (role == ERole.CUSTOMER) {
                    subject = "Your Booking is Pending Confirmation";
                    body = String.format(
                            "<p>Dear Customer,</p>"
                                    + "<p>Your booking for the car <strong>%s</strong> is <strong>waiting for the car owner’s confirmation</strong>.</p>"
                                    + "<p>Booking number: <strong>%s</strong>.</p>"
                                    + "<p>Thank you!</p>",
                            carName, detail);
                } else {
                    subject = "A New Booking Needs Your Confirmation";
                    body = String.format(
                            "<p>Dear Car Owner,</p>"
                                    + "<p>Your car <strong>%s</strong> has been booked.</p>"
                                    + "<p>Please check your account and confirm the booking with the license plate: <strong>%s</strong>.</p>"
                                    + "<p>If you need assistance, please contact support.</p>"
                                    + "<p>Thank you!</p>",
                            carName, detail);
                }
                break;
            case CANCELLED:
                subject = "Your Booking Has Been Cancelled";
                body = String.format(
                        "<p>Dear Customer,</p>"
                                + "<p>Your booking for the car <strong>%s</strong> has been <strong>cancelled</strong> due to: <strong>%s</strong>.</p>"
                                + "<p>Thank you!</p>",
                        carName, detail);
                break;

            default:
                return; // No email needed for other statuses
        }

        sendEmail(to, subject, body);
    }



    //CANCEL BOOKING
    public void sendBookingCancellationEmail(String to, ERole role, EBookingStatus bookingStatus, String carName) throws MessagingException {
        String subject = "";
        String body = "";

        if (role.equals(ERole.CUSTOMER)) {
            subject = "Booking Cancellation Successful";

            switch (bookingStatus) {
                case PENDING_DEPOSIT: // No Refund
                    body = String.format(
                            "<p>Dear Customer,</p>"
                                    + "<p>Your booking for <strong>%s</strong> has been successfully canceled.</p>"
                                    + "<p>If you have any questions, please contact our support team.</p>"
                                    + "<p>Thank you!</p>",
                            carName);
                    break;

                case WAITING_CONFIRMED: // Full Refund
                    subject += " - Full Refund";
                    body = String.format(
                            "<p>Dear Customer,</p>"
                                    + "<p>Your booking for <strong>%s</strong> has been successfully canceled.</p>"
                                    + "<p>Your deposit has been fully refunded.</p>"
                                    + "<p>If you have any questions, please contact our support team.</p>"
                                    + "<p>Thank you!</p>",
                            carName);
                    break;

                case CONFIRMED: // Partial Refund (70%)
                    subject += " - 70% Refund";
                    body = String.format(
                            "<p>Dear Customer,</p>"
                                    + "<p>Your booking for <strong>%s</strong> has been successfully canceled.</p>"
                                    + "<p>You have received a 70%% refund of your deposit.</p>"
                                    + "<p>If you have any questions, please contact our support team.</p>"
                                    + "<p>Thank you!</p>",
                            carName);
                    break;

                default:
                    return; // Do nothing for other statuses
            }

        }
        else if (role.equals(ERole.CAR_OWNER)) {
            subject = "Booking Cancellation Notification";
            body = String.format(
                    "<p>Dear Car Owner,</p>"
                            + "<p>The booking for your car <strong>%s</strong> has been canceled by the customer.</p>"
                            + "<p>The system has retained 30%% of the deposit, and you will receive 22%% of it shortly.</p>"
                            + "<p>Thank you!</p>",
                    carName);
        }

        sendEmail(to, subject, body);
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

    //RETURN CAR
    public void sendPaymentEmail(String to, ERole role, String bookingNumber, long amount, String paymentStatus) throws MessagingException {
        String subject = "";
        String body = "";

        if (role == ERole.CUSTOMER) {
            switch (paymentStatus.toUpperCase()) {
                case "REFUND": // Deposit refunded
                    subject = "Car Returned - Payment Complete (Booking No: " + bookingNumber + ")";
                    body = String.format(
                            "Dear Customer,\n\n"
                                    + "Your car return for Booking No: %s has been processed successfully.\n"
                                    + "Your total payment has been covered by the deposit. An excess amount of $%d has been refunded to your wallet.\n\n"
                                    + "Thank you for choosing our service!\n"
                                    + "Best regards,\n"
                                    + "Your Car Rental Team",
                            bookingNumber, amount);
                    break;

                case "DEDUCT": // Wallet deduction
                    subject = "Car Returned - Payment Deducted (Booking No: " + bookingNumber + ")";
                    body = String.format(
                            "Dear Customer,\n\n"
                                    + "Your car return for Booking No: %s has been processed successfully.\n"
                                    + "Since your deposit was insufficient, an additional amount of $%d has been deducted from your wallet.\n\n"
                                    + "Thank you for choosing our service!\n"
                                    + "Best regards,\n"
                                    + "Your Car Rental Team",
                            bookingNumber, amount);
                    break;

                case "PENDING_PAYMENT": // Customer needs to pay remaining balance
                    subject = "Car Returned - Payment Due (Booking No: " + bookingNumber + ")";
                    body = String.format(
                            "Dear Customer,\n\n"
                                    + "Your car return for Booking No: %s has been processed.\n"
                                    + "However, you have an outstanding balance of $%d.\n"
                                    + "Please complete the payment within 1 hour to avoid penalties.\n\n"
                                    + "Thank you for choosing our service!\n"
                                    + "Best regards,\n"
                                    + "Your Car Rental Team",
                            bookingNumber, amount);
                    break;

                default:
                    return; // No action for other statuses
            }

        } else if (role == ERole.CAR_OWNER && paymentStatus.equalsIgnoreCase("COMPLETED")) {
            subject = "Car Returned - Payment Processed (Booking No: " + bookingNumber + ")";
            body = String.format(
                    "Dear Car Owner,\n\n"
                            + "Your car has been returned for Booking No: %s.\n"
                            + "You have received $%d (92%% of the total rental fee).\n\n"
                            + "Thank you for listing your car with us!\n"
                            + "Best regards,\n"
                            + "Your Car Rental Team",
                    bookingNumber, amount);
        }

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

    //WALLET
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
     * Helper method to get the current date and time in a formatted string.
     * @return Formatted date-time string (dd/MM/yyyy HH:mm).
     */
    private String getCurrentFormattedDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
