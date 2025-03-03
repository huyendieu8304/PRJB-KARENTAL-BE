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
    PENDING_DEPOSIT,
    CONFIRMED, //paid deposit
    CANCELLED,
    IN_PROGRESS,
    PENDING_PAYMENT,
    COMPLETED
}
