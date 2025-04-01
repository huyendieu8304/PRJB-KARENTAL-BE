package com.mp.karental.dto.response.transaction;

import com.mp.karental.constant.ETransactionStatus;
import com.mp.karental.constant.ETransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Create date of transaction", example = "2004-11-23T11:00:00")
    LocalDateTime createdAt;
    @Schema(description = "Transaction Type", example = "REFUND_DEPOSIT")
    ETransactionType type;
    @Schema(
            description = "Booking Number",
            example = "BK202410200001"
    )
    String bookingNo;
    @Schema(
            description = "Car Name(brand + model)",
            example = "Toyota Camry"
    )
    String carName;
    @Schema(
            description = "Amount of the transaction",
            example = "1000000"
    )
    long amount;

    String message;
    @Schema(
            description = "Transaction Status",
            example = "PROCESSING"
    )
    ETransactionStatus status;
}
