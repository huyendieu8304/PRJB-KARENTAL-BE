package com.mp.karental.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ListTransactionResponse {
    long balance;
    List<TransactionResponse> listTransactionResponse;
}
