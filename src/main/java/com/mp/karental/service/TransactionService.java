package com.mp.karental.service;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.TransactionResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Transaction;
import com.mp.karental.entity.Wallet;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.TransactionMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.TransactionRepository;
import com.mp.karental.repository.WalletRepository;
import com.mp.karental.security.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionService {
    TransactionRepository transactionRepository;
    WalletRepository walletRepository;
    AccountRepository accountRepository;
    TransactionMapper transactionMapper;
    public TransactionResponse createTransaction(TransactionRequest transactionRequest) {
        String accountId = SecurityUtil.getCurrentAccountId();
        Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        Transaction transaction = transactionMapper.toTransaction(transactionRequest);
        log.info("Transaction Mapped: {}", transaction);

         Wallet wallet = walletRepository.findByAccountId(accountId);
        if(wallet==null){
                wallet = Wallet.builder()
                        .id(accountId)
                        .account(account)
                        .balance(transaction.getAmount())
                        .build();
            walletRepository.save(wallet);
            }else{
            if(transaction.getType().equals(ETransactionType.WITHDRAW) || transaction.getType().equals(ETransactionType.PAY_DEPOSIT) ) {
                wallet.setBalance(wallet.getBalance()-transaction.getAmount());

            }else{
                wallet.setBalance(wallet.getBalance()+transaction.getAmount());
            }
            walletRepository.save(wallet);
        }
        transaction.setWallet(wallet);
        transactionRepository.save(transaction);
        TransactionResponse transactionResponse =  transactionMapper.toTransactionResponse(transaction);
        transactionResponse.setBalance(wallet.getBalance());
        return transactionResponse;
    }
    public List<TransactionResponse> getAllTransactions( LocalDateTime from, LocalDateTime to) {
        String accountId = SecurityUtil.getCurrentAccountId();
        List<Transaction> transactions= transactionRepository.getTransactionsByDate(accountId, from, to);
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction t: transactions){
            TransactionResponse transactionResponse = transactionMapper.toTransactionResponse(t);
            transactionResponse.setBalance(t.getWallet().getBalance());
            transactionResponses.add(transactionResponse);

        }
        return transactionResponses;
    }
    public List<TransactionResponse> getAllTransactionsList() {
        String accountId = SecurityUtil.getCurrentAccountId();
        List<Transaction> transactions= transactionRepository.getTransactionsByWalletId(accountId);
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction t: transactions){
            TransactionResponse transactionResponse = transactionMapper.toTransactionResponse(t);
            transactionResponse.setBalance(t.getWallet().getBalance());
            transactionResponses.add(transactionResponse);
        }
        return transactionResponses;
    }
}
