package com.mp.karental.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ETransactionType {
    TOP_UP, //Customer, CarOwner
    WITHDRAW, //Customer, CarOwner
    PAY_DEPOSIT, //Customer
    RECEIVE_DEPOSIT, //for car owner
    REFUND_DEPOSIT, //when customer cancels a booking and the deposit is refunded to USER wallet
    OFFSET_FINAL_PAYMENT; // for customer when return car if deposit is left or exceeded
    @JsonCreator
    public static ETransactionType of(String value) {
        return ETransactionType.valueOf(value.toUpperCase());
    }
    @JsonValue
    public String toJson() {
        return name(); // Convert Enum to String when serializing
    }
    }
