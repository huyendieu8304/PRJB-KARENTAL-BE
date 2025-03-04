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
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/wallet")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class TransactionController {
    TransactionService transactionService;
    @GetMapping("/")
    public ApiResponse<List<TransactionResponse>> getAllTransactionResponseList(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to){
        return ApiResponse.<List<TransactionResponse>>builder()
                .data(transactionService.getAllTransactions(from,to))
                .build();
    }
    @PostMapping("/top-up")
    ApiResponse<TransactionResponse> topUp(@ModelAttribute @Valid TransactionRequest transactionRequest) {
        return ApiResponse.<TransactionResponse>builder()
                .data(transactionService.createTransaction(transactionRequest))
                .build();
    }
}
