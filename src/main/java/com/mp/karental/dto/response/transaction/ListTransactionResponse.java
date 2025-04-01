package com.mp.karental.dto.response.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ListTransactionResponse {
    @Schema(description = "Wallet balance", example="100000000")
    long balance;
    @Schema(description = "List of Transaction", implementation = TransactionResponse.class)
    List<TransactionResponse> listTransactionResponse;
}
