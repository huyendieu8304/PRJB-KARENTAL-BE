package com.mp.karental.controller;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.ListTransactionResponse;
import com.mp.karental.dto.response.TransactionPaymentURLResponse;
import com.mp.karental.dto.response.TransactionResponse;
import com.mp.karental.entity.Transaction;
import com.mp.karental.service.TransactionService;
import com.mp.karental.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class TransactionController {
    TransactionService transactionService;
    @GetMapping(value="/transaction-list", params = { "from", "to" })
    public ApiResponse<ListTransactionResponse> getAllTransactionResponseList(@RequestParam(name = "from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                                @RequestParam(name = "to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){
        return ApiResponse.<ListTransactionResponse>builder()
                .data(transactionService.getAllTransactions(from,to))
                .build();
    }
    @GetMapping(value="/transaction-list", params = "all")
    public ApiResponse<ListTransactionResponse> getAllTransactionList(@RequestParam boolean all){
        if (Boolean.TRUE.equals(all)) {
        return ApiResponse.<ListTransactionResponse>builder()
                .data(transactionService.getAllTransactionsList())
                .build();
    }
    return ApiResponse.<ListTransactionResponse>builder()
            .data(new ListTransactionResponse()) // Return empty list if `all` is false or null
            .build();

    }
    @PostMapping("/top-up")
    ApiResponse<TransactionPaymentURLResponse> topUp(@RequestBody @Valid TransactionRequest transactionRequest, HttpServletRequest httpServletRequest) {
        var ipAddress = RequestUtil.getIpAddress(httpServletRequest);
        transactionRequest.setIpAddress(ipAddress);
        log.info("Transaction Request: {}", transactionRequest);
        transactionRequest.setType(ETransactionType.TOP_UP);
        return ApiResponse.<TransactionPaymentURLResponse>builder()
                .data(transactionService.createTransaction(transactionRequest))
                .build();
    }
    @PostMapping("/withdraw")
    ApiResponse<TransactionPaymentURLResponse> withdraw(@RequestBody @Valid TransactionRequest transactionRequest, HttpServletRequest httpServletRequest) {
        var ipAddress = RequestUtil.getIpAddress(httpServletRequest);
        transactionRequest.setIpAddress(ipAddress);
        log.info("Transaction Request: {}", transactionRequest);
        transactionRequest.setType(ETransactionType.WITHDRAW);
        return ApiResponse.<TransactionPaymentURLResponse>builder()
                .data(transactionService.createTransaction(transactionRequest))
                .build();
    }
    @GetMapping("{transactionId}/status")
    ApiResponse<TransactionResponse> getTransaction(@PathVariable String transactionId, @RequestParam Map<String, String> params) {
            return ApiResponse.<TransactionResponse>builder()
                    .data(transactionService.getTransactionStatus(transactionId, params))
                    .build();
    }


}
