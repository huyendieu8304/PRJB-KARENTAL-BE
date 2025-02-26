package com.mp.karental.constant;

import lombok.Getter;

@Getter
public enum EBookingStatus {
    PENDING_DEPOSIT,
    CONFIRMED, //paid deposit
    CANCELLED,
    IN_PROGRESS,
    PENDING_PAYMENT,
    COMPLETED
}
