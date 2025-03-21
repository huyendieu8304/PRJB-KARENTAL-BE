package com.mp.karental.dto.request.transaction;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.validation.ValidTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionRequest {
    @ValidTransactionType(message = "INVALID_TRANSACTION_TYPE")
    @NotNull
    ETransactionType type;

    String bookingNo;

     String carName;

    long amount;

    String message;

    private String ipAddress;
}
