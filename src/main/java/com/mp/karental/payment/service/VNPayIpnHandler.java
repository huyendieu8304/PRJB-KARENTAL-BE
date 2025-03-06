package com.mp.karental.payment.service;

import com.mp.karental.exception.AppException;
import com.mp.karental.payment.constant.VNPayIPNResponseConst;
import com.mp.karental.payment.constant.VNPayParams;
import com.mp.karental.payment.dto.response.IpnResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VNPayIpnHandler  implements IpnHandler{

    private final VNPayService vnPayService;



    @Override
    public IpnResponse process(Map<String, String> params) {
        if(!vnPayService.verifyIpn(params)){
            return VNPayIPNResponseConst.SIGNATURE_FAILED;
        }
        IpnResponse  ipnResponse ;
        var txnRef = params.get(VNPayParams.TXN_REF);
        try{
            var transactionId = txnRef.toString();
            ipnResponse = VNPayIPNResponseConst.SUCCESS;
        }catch(Exception e){
            ipnResponse = VNPayIPNResponseConst.SIGNATURE_FAILED;
        }
        return ipnResponse;
    }
}
