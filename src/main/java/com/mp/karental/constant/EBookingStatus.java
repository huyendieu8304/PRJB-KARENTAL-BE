package com.mp.karental.constant;

import lombok.Getter;

/**
 * Represents the status of a booking in the system
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Getter
public enum EBookingStatus {
    PENDING_DEPOSIT, //booked but hasn't paid deposit yet
    WAITING_CONFIRM,
    CONFIRMED, //paid deposit
    CANCELLED, //cancelled booking
    IN_PROGRESS, //customer picked up car
    PENDING_PAYMENT, //customer returned car but hasn't complete payment
    COMPLETED //returned car and completed payment
}
