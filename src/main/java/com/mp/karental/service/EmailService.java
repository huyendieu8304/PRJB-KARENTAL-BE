package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
     *
     * @param to         Recipient's email address.
     * @param confirmUrl URL for email verification.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendRegisterEmail(String to, String confirmUrl) {
        String subject = "Welcome to Karental, " + to;
        String htmlContent = "<p><strong>Thank you for registering to our system!</strong></p>"
                + "<p>To continue using our services, please verify your email by clicking the link below:</p>"
                + "<p><a href=\"" + confirmUrl + "\" style=\"color: blue; font-weight: bold;\">Verify Email</a></p>"
                + "<p>If you did not sign up for this service, please ignore this email.</p>";

        try {
            sendEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_VERIFY_EMAIL_TO_USER_FAIL);
        }
    }

    //FORGOT PASSWORD

    /**
     * Sends a password reset email.
     *
     * @param to                Recipient's email address.
     * @param forgotPasswordUrl URL for password reset.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendForgotPasswordEmail(String to, String forgotPasswordUrl) {
        String subject = "Rent-a-car Password Reset";
        String htmlContent = "<p>We have just received a password reset request for " + to + ".</p>"
                + "<p>Please click <a href=\"" + forgotPasswordUrl + "\">here</a> to reset your password.</p>"
                + "<p>For your security, the link will expire in 24 hours or immediately after you reset your password.</p>";
        try {
            sendEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_FORGOT_PASSWORD_EMAIL_TO_USER_FAIL);
        }
    }

    //RENT CAR
    public void sendWaitingConfirmedEmail(String toCustomer, String toCarOwner, String carName, String bookingNumber) {
        String subjectToCustomer = "Your Booking is waiting for confirmation";
        String bodyToCustomer = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>You has successfully paid deposit for the booking of the car <strong>%s</strong>." +
                        " Your booking is <strong>waiting for the car owner’s confirmation</strong>.</p>"
                        + "<p>Booking number: <strong>%s</strong>.</p>"
                        + "<p>Thank you!</p>",
                carName, bookingNumber);

        String subjectToCarOwner = "A New Booking Needs Your Confirmation";
        String bodyToCarOwner = String.format(
                "<p>Dear Car Owner,</p>"
                        + "<p>Your car <strong>%s</strong> has been booked.</p>"
                        + "<p>Please check your account and confirm the booking <strong>%s</strong>.</p>"
                        + "<p>If you need assistance, please contact support.</p>"
                        + "<p>Thank you!</p>",
                carName, bookingNumber);

        try {
            sendEmail(toCustomer, subjectToCustomer, bodyToCustomer);
            sendEmail(toCarOwner, subjectToCarOwner, bodyToCarOwner);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_WAITING_CONFIRM_EMAIL_FAIL);
        }
    }

    public void sendCancelledBookingEmail(String toCustomer, String carName, String reason) {
        String subject = "Your Booking Has Been Cancelled";
        String body = String.format(
                "<p>Dear Customer,</p>"
                        + "<p>Your booking for the car <strong>%s</strong> has been <strong>cancelled</strong> due to: <strong>%s</strong>.</p>"
                        + "<p>Thank you!</p>",
                carName, reason);
        try {
            sendEmail(toCustomer, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_CANCELLED_BOOKING_EMAIL_FAIL);
        }
    }


    //    //CANCEL BOOKING
    public void sendBookingCancellationEmailToCustomer(String to, EBookingStatus bookingStatus, String bookingNumber, String carName) {
        String subject = "";
        String body = "";

        subject = "Booking Cancellation Successful";

        switch (bookingStatus) {
            case PENDING_DEPOSIT: // No Refund
                body = String.format(
                        "<p>Dear Customer,</p>"
                                + "<p>Your booking <strong>%s</strong> for <strong>%s</strong> has been successfully canceled.</p>"
                                + "<p>If you have any questions, please contact our support team.</p>"
                                + "<p>Thank you!</p>",
                        bookingNumber, carName);
                break;

            case WAITING_CONFIRMED: // Full Refund
                subject += " - Full Refund";
                body = String.format(
                        "<p>Dear Customer,</p>"
                                + "<p>Your booking <strong>%s</strong> for <strong>%s</strong> has been successfully canceled.</p>"
                                + "<p>Your deposit has been fully refunded.</p>"
                                + "<p>If you have any questions, please contact our support team.</p>"
                                + "<p>Thank you!</p>",
                        bookingNumber, carName);
                break;

            case CONFIRMED: // Partial Refund (70%)
                subject += " - 70% Refund";
                body = String.format(
                        "<p>Dear Customer,</p>"
                                + "<p>Your booking <strong>%s</strong> for <strong>%s</strong> has been successfully canceled.</p>"
                                + "<p>You have received a 70%% refund of your deposit.</p>"
                                + "<p>If you have any questions, please contact our support team.</p>"
                                + "<p>Thank you!</p>",
                        bookingNumber, carName);
                break;

            default:
                return; // Do nothing for other statuses
        }
        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_CANCELLED_BOOKING_EMAIL_FAIL);
        }
    }

    public void sendBookingCancellationEmailToCarOwner(String to, String bookingNumber, String carName) {
        String subject = "Booking Cancellation Notification";
        String body = String.format(
                "<p>Dear Car Owner,</p>"
                        + "<p>The booking <strong>%s</strong> for your car <strong>%s</strong> has been canceled by the customer.</p>"
                        + "<p>You will receive 22%% deposit of the booking it shortly.</p>"
                        + "<p>Thank you!</p>",
                bookingNumber, carName);
        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_CANCELLED_BOOKING_EMAIL_FAIL);
        }
    }

    //CONFIRM PICKUP

    /**
     * Sends a confirmation email to the customer regarding their car booking pickup.
     *
     * @param to            The recipient's email address (customer).
     * @param carName       The name of the booked car.
     * @param bookingNumber The unique booking number.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendConfirmBookingEmail(String to, String carName, String bookingNumber) {
        // Email subject including the booking number
        String subject = "Booking Confirmed - " + bookingNumber;

        // Email body with booking details and a request for the customer to confirm the pickup
        String body = "Dear Customer,\n\n"
                + "Your booking (Booking No: " + bookingNumber + ") for the car " + carName + " has been confirmed.\n"
                + "When you pick up the car, please confirm it in the system.\n\n"
                + "Thank you for choosing our service!\n"
                + "Best regards,\n";

        // Sending the email
        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_CONFIRMED_BOOKING_EMAIL_FAIL);
        }
    }

    //RETURN CAR
    public void sendPaymentEmailToCustomer(String to, String bookingNumber, long amount, boolean isRefund)  {
        String subject = "Car Returned - Payment Deducted (Booking No: " + bookingNumber + ")";
        String body = String.format(
                "Dear Customer,\n\n"
                        + "Your car return for Booking No: %s has been processed successfully.\n"
                        + "Since your deposit was insufficient, an additional amount of $%d has been deducted from your wallet.\n\n"
                        + "Thank you for choosing our service!\n"
                        + "Best regards,\n"
                        + "Your Car Rental Team",
                bookingNumber, amount);

        if (isRefund) {
            subject = "Car Returned - Payment Complete (Booking No: " + bookingNumber + ")";
            body = String.format(
                    "Dear Customer,\n\n"
                            + "Your car return for Booking No: %s has been processed successfully.\n"
                            + "Your total payment has been covered by the deposit. An excess amount of $%d has been refunded to your wallet.\n\n"
                            + "Thank you for choosing our service!\n"
                            + "Best regards,\n"
                            + "Your Car Rental Team",
                    bookingNumber, amount);
        }


        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_COMPLETED_BOOKING_EMAIL_FAIL);
        }
    }

    public void sendPaymentEmailToCarOwner(String to, String bookingNumber, long amount) {
        String subject = "Car Returned - Payment Processed (Booking No: " + bookingNumber + ")";
        String body = String.format(
                "Dear Car Owner,\n\n"
                        + "Your car has been returned for Booking No: %s.\n"
                        + "You have received $%d (92%% of the total rental fee).\n\n"
                        + "Thank you for listing your car with us!\n"
                        + "Best regards,\n",
                bookingNumber, amount);
        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_COMPLETED_BOOKING_EMAIL_FAIL);
        }
    }


    public void sendPendingPaymentEmail(String to, String bookingNumber, long amount) {
        String subject = "Car Returned - Payment Due (Booking No: " + bookingNumber + ")";
        String body = String.format(
                "Dear Customer,\n\n"
                        + "Your car return for Booking No: %s has been processed.\n"
                        + "However, you have an outstanding balance of $%d.\n"
                        + "Please complete the payment within 1 hour to avoid penalties.\n\n"
                        + "Thank you for choosing our service!\n"
                        + "Best regards,\n"
                        + "Your Car Rental Team",
                bookingNumber, amount);
        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_PENDING_PAYMENT_BOOKING_EMAIL_FAIL);
        }
    }


    /**
     * Helper method to send an email with the given parameters.
     *
     * @param to          Recipient's email address.
     * @param subject     Email subject.
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
     *
     * @param to        Recipient's email address.
     * @param walletUrl URL to view the wallet balance and transactions.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendWalletUpdateEmail(String to, String walletUrl){
        String subject = "There’s an update to your wallet";
        String htmlContent = String.format(
                "<p>Please be informed that your wallet’s balance has been updated at <strong>%s</strong>.</p>"
                        + "<p>Please go to your <a href=\"%s\">wallet</a> and view the transactions for more details.</p>"
                        + "<p>Thank you!</p>",
                getCurrentFormattedDateTime(), walletUrl);
        try {
            sendEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_WALLET_UPDATE_EMAIL_FAIL);
        }
    }

    /**
     * Helper method to get the current date and time in a formatted string.
     *
     * @return Formatted date-time string (dd/MM/yyyy HH:mm).
     */
    private String getCurrentFormattedDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }


    /**
     * Sends an email notification to the car owner when their car is successfully verified.
     *
     * @param to      Recipient's email address (car owner's email).
     * @param carName The name of the car that has been verified.
     * @param carId   The unique identifier of the verified car.
     */
    public void sendCarVerificationEmail(String to, String carName, String carId) {
        String subject = "Car Verification Approved - " + carName;
        String body = String.format(
                "<p>Dear Car Owner,</p>"
                        + "<p>Your car <strong>%s</strong> (ID: %s) has been successfully verified.</p>"
                        + "<p>You can now list your car for rental on our platform.</p>"
                        + "<p>Thank you for choosing our service!</p>",
                carName, carId);

        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_CAR_VERIFICATION_EMAIL_FAIL);
        }
    }

}
