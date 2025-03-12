package com.mp.karental.service;

import com.mp.karental.constant.ETransactionStatus;
import com.mp.karental.constant.ETransactionType;
import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.ListTransactionResponse;
import com.mp.karental.dto.response.TransactionPaymentURLResponse;
import com.mp.karental.dto.response.TransactionResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Transaction;
import com.mp.karental.entity.Wallet;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.TransactionMapper;
import com.mp.karental.payment.constant.VNPayIPNResponseConst;
import com.mp.karental.payment.dto.request.InitPaymentRequest;
import com.mp.karental.payment.dto.response.InitPaymentResponse;
import com.mp.karental.payment.dto.response.IpnResponse;
import com.mp.karental.payment.service.IpnHandler;
import com.mp.karental.payment.service.PaymentService;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.TransactionRepository;
import com.mp.karental.repository.WalletRepository;
import com.mp.karental.security.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    PaymentService paymentService;
    private final ApplicationRunner init;
    IpnHandler ipnHandler;
    public TransactionPaymentURLResponse createTransaction(TransactionRequest transactionRequest) {
        log.info("Creating Transaction: {}", transactionRequest);

        // When create transaction, save it to database with status PROCESSING
        String accountId = SecurityUtil.getCurrentAccountId();
        Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        Transaction transaction = transactionMapper.toTransaction(transactionRequest);
        log.info("Transaction request: {}", transactionRequest);
         Wallet wallet = walletRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        transaction.setWallet(wallet);
        transaction.setStatus(ETransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        TransactionResponse transactionResponse =  transactionMapper.toTransactionResponse(transaction);
        // if the transaction is top-up, initialize a payment request
        if(transaction.getType().equals(ETransactionType.TOP_UP)){
            InitPaymentRequest initPaymentRequest = InitPaymentRequest.builder()
                    .transactionType(transaction.getType())
                    .userId(accountId)
                    .requestId(transaction.getId())
                    .amount(transaction.getAmount())
                    .ipAddress(transactionRequest.getIpAddress())
                    // .txnRef(transactionRequest.getBookingNo())
                    .txnRef(transaction.getId())
                    .build();
            // return response include payment url, front-end will redirect
            InitPaymentResponse initPaymentResponse = paymentService.initPayment(initPaymentRequest);
            return TransactionPaymentURLResponse.builder()
                    .transactionResponse(transactionResponse)
                    .payment(initPaymentResponse)
                    .build();
        }
        // if transaction is withdraw, directly deduct balance
        if(transaction.getType().equals(ETransactionType.WITHDRAW)){
            // if amount is larger than balance, throw error
            if(wallet.getBalance() < transaction.getAmount()){
                transaction.setStatus(ETransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new AppException(ErrorCode.PAYMENT_FAILED);
            }else {
                transaction.setStatus(ETransactionStatus.SUCCESSFUL);
                wallet.setBalance(wallet.getBalance() - transaction.getAmount());
                transactionRepository.save(transaction);
            }
        }
        transactionResponse =  transactionMapper.toTransactionResponse(transaction);
        log.info("Transaction withdraw response: {}", transactionResponse);
        // return response for Withdraw transaction
        return TransactionPaymentURLResponse.builder()
                .transactionResponse(transactionResponse)
                .build();
    }
    //get the transaction status after vnpay process
    public TransactionResponse getTransactionStatus(String transactionId, Map<String,String> params) {
        log.info("Checking Transaction Status: transactionId={}, params={}", transactionId, params);
        IpnResponse ipnResponse = ipnHandler.process(params);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_IN_DB));
        String accountId = SecurityUtil.getCurrentAccountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        Wallet wallet = walletRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        TransactionResponse transactionResponse = transactionMapper.toTransactionResponse(transaction);
        log.info("Transaction Status: transactionResponse={}", transactionResponse);
        // if transaction status is PROCESSING
        if (!transaction.getStatus().equals(ETransactionStatus.SUCCESSFUL) &&
                !transaction.getStatus().equals(ETransactionStatus.FAILED)) {
            // if response from vnpay returnUrl is success
            if (ipnResponse.getResponseCode().equals(VNPayIPNResponseConst.SUCCESS.getResponseCode())) {
                if (transaction.getType().equals(ETransactionType.TOP_UP)) {
                    // update transaction status to SUCCESSFUL
                transactionResponse.setStatus(ETransactionStatus.SUCCESSFUL);
                transaction.setStatus(ETransactionStatus.SUCCESSFUL);
                log.info("Updating transaction to SUCCESSFUL: transactionId={}", transaction.getId());
                    // set balance for wallet
                    wallet.setBalance(wallet.getBalance() + transaction.getAmount());
                }
                //save balance
                walletRepository.save(wallet);
            }
            // if vnpay response is failed
            else {
                //update transaction status to FAILED
                transactionResponse.setStatus(ETransactionStatus.FAILED);
                transaction.setStatus(ETransactionStatus.FAILED);
                //throw exception
                throw new AppException(ErrorCode.VNPAY_PAYMENT_FAILED);
            }
            // save exception to db even if it's failed
            transactionRepository.save(transaction);
        }
        return transactionResponse;
    }
    // method serves as third-party, customer will transfer money from wallet to admin wallet
    public void payDeposit(String customerId, long amount, Booking b ){
        //Find 2 user wallet
        Wallet customerWallet = walletRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        Wallet adminWallet = walletRepository.findById(accountRepository.findByRoleId(3).getId())
                                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        // deduct from customer wallet
        customerWallet.setBalance(customerWallet.getBalance() - amount);
        // plus to admin wallet
        adminWallet.setBalance(adminWallet.getBalance() + amount);
        walletRepository.save(customerWallet);
        walletRepository.save(adminWallet);
        // save as new transaction
        Transaction transaction = Transaction.builder()
                                    .type(ETransactionType.PAY_DEPOSIT)
                                    .amount(amount)
                                    .bookingNo(b.getBookingNumber())
                                    .carName(b.getCar().getModel())
                                    .status(ETransactionStatus.SUCCESSFUL)
                                    .wallet(customerWallet)
                                    .build();
        transactionRepository.save(transaction);
    }
    // method serves as third-party, admin will transfer money from wallet to car owner wallet
    public void payForCarOwner(String carOwnerId, long amount, Booking b, float benefitRateForSystem ){
       //Find 2 user wallet
        Wallet carOwnerWallet = walletRepository.findById(carOwnerId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        Wallet adminWallet = walletRepository.findById(accountRepository.findByRoleId(3).getId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        //modify this with business rule of system
        // plus to car owner wallet
        carOwnerWallet.setBalance((long) (carOwnerWallet.getBalance() + amount*(1-benefitRateForSystem)));
        // deduct from admin wallet
        adminWallet.setBalance(adminWallet.getBalance() - amount);

        walletRepository.save(carOwnerWallet);
        walletRepository.save(adminWallet);
        // save as new transaction
        Transaction transaction = Transaction.builder()
                .type(ETransactionType.RECEIVE_PAYMENT)
                .amount(amount)
                .bookingNo(b.getBookingNumber())
                .carName(b.getCar().getModel())
                .status(ETransactionStatus.SUCCESSFUL)
                .wallet(carOwnerWallet)
                .build();
        transactionRepository.save(transaction);
    }
    //get transaction with date
    public ListTransactionResponse getAllTransactions(LocalDateTime from, LocalDateTime to) {
        String accountId = SecurityUtil.getCurrentAccountId();
        // call from repository
        List<Transaction> transactions= transactionRepository.getTransactionsByDate(accountId, from, to);
        Wallet wallet = walletRepository.findById(accountId).get();
        // init a new list of transaction response
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction t: transactions){
            TransactionResponse transactionResponse = transactionMapper.toTransactionResponse(t);
            transactionResponses.add(transactionResponse);
        }
        // this response include wallet balance
        ListTransactionResponse listTransactionResponse = new ListTransactionResponse();
        listTransactionResponse.setBalance(wallet.getBalance());
        listTransactionResponse.setListTransactionResponse(transactionResponses);
        return listTransactionResponse;
    }
    //get transaction without date (all)
    public ListTransactionResponse getAllTransactionsList() {
        String accountId = SecurityUtil.getCurrentAccountId();
        List<Transaction> transactions= transactionRepository.getTransactionsByWalletId(accountId);
        Wallet wallet = walletRepository.findById(accountId).get();
        // init new list of transaction response
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction t: transactions){
            TransactionResponse transactionResponse = transactionMapper.toTransactionResponse(t);
            transactionResponses.add(transactionResponse);
        }
        // this response include wallet balance
        ListTransactionResponse listTransactionResponse = new ListTransactionResponse();
        listTransactionResponse.setBalance(wallet.getBalance());
        listTransactionResponse.setListTransactionResponse(transactionResponses);
        return listTransactionResponse;
    }
}
