package com.mp.karental.payment.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* This class act like a dto request that takes payment information of the transaction*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
public class InitPaymentRequest {
    private String requestId;
    private String ipAddress;
    private String userId;
    private String txnRef;
    private long amount;

}
