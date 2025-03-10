package com.mp.karental.payment.service;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.payment.constant.VNPayIPNResponseConst;
import com.mp.karental.payment.constant.VNPayParams;
import com.mp.karental.payment.dto.response.IpnResponse;
import com.mp.karental.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VNPayIpnHandler  implements IpnHandler{

    private final VNPayService vnPayService;
    private final TransactionRepository transactionRepository;


    @Override
    public IpnResponse process(Map<String, String> params) {
        if(!vnPayService.verifyIpn(params)){
            throw new AppException(ErrorCode.VNPAY_CHECKSUM_FAILED);
        }
        IpnResponse  ipnResponse ;
        var txnRef = params.get(VNPayParams.TXN_REF);
        try {
            transactionRepository.findById(txnRef);
            ipnResponse = VNPayIPNResponseConst.SUCCESS;
        }
        catch (AppException e) {
            switch (e.getErrorCode()) {
               // case _NOT_FOUND -> response = VnpIpnResponseConst.ORDER_NOT_FOUND;
                default -> ipnResponse = VNPayIPNResponseConst.UNKNOWN_ERROR;
            }
        }
        catch (Exception e) {
            ipnResponse = VNPayIPNResponseConst.UNKNOWN_ERROR;
        }
        return ipnResponse;
    }
}
