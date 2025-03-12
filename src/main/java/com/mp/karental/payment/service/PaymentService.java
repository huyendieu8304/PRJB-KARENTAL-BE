package com.mp.karental.payment.service;

import com.mp.karental.payment.dto.request.InitPaymentRequest;
import com.mp.karental.payment.dto.response.InitPaymentResponse;

public interface PaymentService {
    InitPaymentResponse initPayment(InitPaymentRequest request);
}
