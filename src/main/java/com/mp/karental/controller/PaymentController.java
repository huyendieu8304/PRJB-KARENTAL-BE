package com.mp.karental.controller;

import com.mp.karental.payment.dto.response.IpnResponse;
import com.mp.karental.payment.service.IpnHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//This class is used to get the response from VNPAY  to know that the secure hash is correct or not

@RestController
@Slf4j
@RequestMapping ("/api/v1/payments")
@RequiredArgsConstructor

public class PaymentController {

    private final IpnHandler ipnHandler;
    @GetMapping("/vnpay_ipn")
    IpnResponse processIpn(@RequestParam Map<String, String> params) {
        log.info("[VNPay Ipn] Params: {}", params);
        return ipnHandler.process(params);
    }
}
