package com.mp.karental.constant;

import lombok.Getter;

/**
 * Represents the transaction type of transaction in user's wallet
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Getter
public enum ETransactionType {
    TOP_UP,
    WITHDRAW,
    PAYMENT //pay for booking
}
