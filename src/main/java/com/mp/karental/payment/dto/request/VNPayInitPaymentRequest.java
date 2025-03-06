package com.mp.karental.payment.dto.request;
/* This class acts like a request initialization to request to VNPay environment*/
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayInitPaymentRequest {
    // According to VNPay guide
    @JsonProperty("vnp_Version")
    public static final String VERSION = "2.1.0";
    // According to VNPay guide
    @JsonProperty("vnp_Version")
    public static final String COMMAND = "pay";

    @JsonProperty("vnp_RequestId")
    private String requestId;

    @JsonProperty("vnp_TmnCode")
    private String tmnCode;

    @JsonProperty("vnp_TxnRef")
    private String txnRef;

    @JsonProperty("vnp_CreateDate")
    private String createdDate;

    @JsonProperty("vnp_IpAddr")
    private String ipAddress;

    @JsonProperty("vnp_OrderInfo")
    private String orderInfo;

    @JsonProperty("vnp_SecureHash")
    private String secureHash;
// The @ JsonProperty is used as a setter or getter deriver of the {"ancestor"} and use the name of getter setter
}
