package com.mp.karental.payment.constant;

import com.mp.karental.payment.dto.response.IpnResponse;

// status of IpnResponse(when check hash sequence)
public class VNPayIPNResponseConst {
    public static final IpnResponse SUCCESS = new IpnResponse("00", "Successful");
    public static final IpnResponse SIGNATURE_FAILED = new IpnResponse("97", "Signature failed");
    public static final IpnResponse ORDER_NOT_FOUND = new IpnResponse("01", "Order not found");
    public static final IpnResponse UNKNOWN_ERROR = new IpnResponse("99", "Unknown error");
}
