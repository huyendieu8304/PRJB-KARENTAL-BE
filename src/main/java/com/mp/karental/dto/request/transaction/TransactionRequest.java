package com.mp.karental.dto.request.transaction;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.validation.ValidTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionRequest {
    @ValidTransactionType(message = "INVALID_TRANSACTION_TYPE")
    @NotNull
    @Schema(
            description = "Transaction Type",
            example = "WITHDRAW"
    )
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
            description = "Amount of the transaction",
            example = "127.2.0.34"
    )
    private String ipAddress;
}
