package com.mp.karental.payment.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// Use for return a response url (in local)

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class InitPaymentResponse {
        @Schema(description = "The url that vnpay generated for us", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=200000000&vnp_Command=refund&vnp_CreateDate=20250309124139&vnp_CurrCode=VND&vnp_ExpireDate=20250309124239&vnp_IpAddr=0%3A0%3A0%3A0%3A0%3A0%3A0%3A1&vnp_Locale=en&vnp_OrderInfo=Withdraw+transacti")
        private String vnpUrl;
}
