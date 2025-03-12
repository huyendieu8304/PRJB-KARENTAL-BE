package com.mp.karental.payment.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/*
 This class is for configure vnpay in yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment.vnpay")
public class PaymentConfig {
    private String tmnCode;
    private String secretKey;
    private String initPaymentUrl;
    private String returnUrl;
    private int timeout;
}


