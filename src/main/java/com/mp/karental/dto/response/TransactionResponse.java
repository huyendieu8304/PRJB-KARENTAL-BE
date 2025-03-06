package com.mp.karental.dto.response;

import com.mp.karental.constant.ETransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionResponse {
    LocalDateTime createdAt;
    ETransactionType type;
    String bookingNo;
    String carName;
    long amount;
    long balance;
    String message;
}
