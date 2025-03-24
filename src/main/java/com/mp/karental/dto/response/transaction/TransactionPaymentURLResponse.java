package com.mp.karental.dto.response.transaction;

import com.mp.karental.payment.dto.response.InitPaymentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionPaymentURLResponse {
    TransactionResponse transactionResponse;
    InitPaymentResponse payment;
}
