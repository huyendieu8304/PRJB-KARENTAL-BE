package com.mp.karental.controller;

import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.TransactionResponse;
import com.mp.karental.entity.Transaction;
import com.mp.karental.service.TransactionService;
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

@RestController
@RequestMapping("/my-wallet")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class TransactionController {
    TransactionService transactionService;
    @GetMapping(value="/transaction-list", params = { "from", "to" })
    public ApiResponse<List<TransactionResponse>> getAllTransactionResponseList(@RequestParam(name = "from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                                @RequestParam(name = "to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){
        return ApiResponse.<List<TransactionResponse>>builder()
                .data(transactionService.getAllTransactions(from,to))
                .build();
    }
    @GetMapping(value="/transaction-list", params = "all")
    public ApiResponse<List<TransactionResponse>> getAllTransactionList(@RequestParam boolean all){
        if (Boolean.TRUE.equals(all)) {
        return ApiResponse.<List<TransactionResponse>>builder()
                .data(transactionService.getAllTransactionsList())
                .build();
    }
    return ApiResponse.<List<TransactionResponse>>builder()
            .data(new ArrayList<>()) // Return empty list if `all` is false or null
            .build();

    }
    @PostMapping("/top-up")
    ApiResponse<TransactionResponse> topUp(@RequestBody @Valid TransactionRequest transactionRequest) {
        return ApiResponse.<TransactionResponse>builder()
                .data(transactionService.createTransaction(transactionRequest))
                .build();
    }


}
