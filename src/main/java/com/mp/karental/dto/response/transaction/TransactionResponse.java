package com.mp.karental.dto.response.transaction;

import com.mp.karental.constant.ETransactionStatus;
import com.mp.karental.constant.ETransactionType;
import com.mp.karental.payment.dto.response.InitPaymentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionResponse {
    @CreationTimestamp
    LocalDateTime createdAt;

    ETransactionType type;

    String bookingNo;

    String carName;

    long amount;

    String message;

    ETransactionStatus status;
}
