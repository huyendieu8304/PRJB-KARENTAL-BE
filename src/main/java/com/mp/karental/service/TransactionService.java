package com.mp.karental.service;

import com.mp.karental.constant.ETransactionStatus;
import com.mp.karental.constant.ETransactionType;
import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.ListTransactionResponse;
import com.mp.karental.dto.response.TransactionPaymentURLResponse;
import com.mp.karental.dto.response.TransactionResponse;
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

import java.time.Duration;
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
    // method serve withdraw transaction

    //create payment URL to VNPAY sanbox when transaction is TOP_UP
    public TransactionPaymentURLResponse createTransactionTopUp(TransactionRequest transactionRequest) {
        log.info("Creating Transaction: {}", transactionRequest);

        // When create transaction, save it to database with status PROCESSING
        String accountId = SecurityUtil.getCurrentAccountId();

        Transaction transaction = transactionMapper.toTransaction(transactionRequest);
        Wallet wallet = walletRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        transaction.setWallet(wallet);
        transaction.setStatus(ETransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        TransactionResponse transactionResponse = transactionMapper.toTransactionResponse(transaction);
        // if the transaction is top-up, initialize a payment request
        if (transaction.getType().equals(ETransactionType.TOP_UP)) {
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
        if (transaction.getType().equals(ETransactionType.WITHDRAW)) {
            // if amount is larger than balance, throw error
            if (wallet.getBalance() < transaction.getAmount()) {
                transaction.setStatus(ETransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new AppException(ErrorCode.PAYMENT_FAILED);
            } else {
                transaction.setStatus(ETransactionStatus.SUCCESSFUL);
                wallet.setBalance(wallet.getBalance() - transaction.getAmount());
                transactionRepository.save(transaction);
            }
        }
        walletRepository.save(wallet);
        transactionResponse = transactionMapper.toTransactionResponse(transaction);
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

    // Method to transfer money from admin wallet to user wallet (include customer and car owner)
    private Wallet transferFromSystemToUser(String userId, long amount){
        Wallet userWallet = walletRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        Wallet adminWallet = walletRepository.findById(accountRepository.findByRoleId(3).getId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        // deduct from admin wallet
        adminWallet.setBalance(adminWallet.getBalance() - amount);
        // plus to user wallet
        userWallet.setBalance(userWallet.getBalance() + amount);
        //save wallet
        walletRepository.save(userWallet);
        walletRepository.save(adminWallet);
        return userWallet;
    }


    // Method to transfer money from user wallet (include customer and car owner) to admin wallet
    private Wallet transferFromUserToSystem(String userId, long amount){
        Wallet userWallet = walletRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        Wallet adminWallet = walletRepository.findById(accountRepository.findByRoleId(3).getId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND_IN_DB));
        // deduct from customer wallet
        userWallet.setBalance(userWallet.getBalance() - amount);
        // plus to admin wallet
        adminWallet.setBalance(adminWallet.getBalance() + amount);
        //save wallet
        walletRepository.save(userWallet);
        walletRepository.save(adminWallet);
        return userWallet;
    }

    // method serves as third-party, customer will transfer money from wallet to admin wallet when pay deposit
    public void payDeposit(Booking b ){
        String customerId = b.getAccount().getId();
        // customer pay for system
        Wallet customerWallet =  transferFromUserToSystem(customerId,b.getDeposit());
        // save as new transaction
        Transaction transaction = Transaction.builder()
                                    .type(ETransactionType.PAY_DEPOSIT)
                                    .amount(b.getDeposit())
                                    .bookingNo(b.getBookingNumber())
                                    .carName(b.getCar().getModel())
                                    .status(ETransactionStatus.SUCCESSFUL)
                                    .wallet(customerWallet)
                                    .build();
        transactionRepository.save(transaction);
    }

    /* method serves as third-party,
     admin will transfer money from wallet to user wallet when
     user cancel booking (car owner: 22% and customer: 70%, system get 8%)
    */
    public void refundPartialDeposit (Booking b){
       //Find id of customer, car owner
        String customerId = b.getAccount().getId();
        String carOwnerId = b.getCar().getAccount().getId();
        //system pay for customer 70%
        Wallet customerWallet = transferFromSystemToUser(customerId,(long)(b.getDeposit()*0.7));
        //system pay for customer 22%
        Wallet carOwnerWallet = transferFromSystemToUser(carOwnerId,(long)(b.getDeposit()*0.22));

        // save as new transaction for customer
        Transaction transactionCustomer = Transaction.builder()
                .type(ETransactionType.REFUND_DEPOSIT)
                .amount((long)(b.getDeposit()*0.7))
                .bookingNo(b.getBookingNumber())
                .carName(b.getCar().getModel())
                .status(ETransactionStatus.SUCCESSFUL)
                .wallet(customerWallet)
                .build();
        transactionRepository.save(transactionCustomer);
        // save as new transaction for carOwner
        Transaction transactionCarOwner = Transaction.builder()
                .type(ETransactionType.REFUND_DEPOSIT)
                .amount((long)(b.getDeposit()*0.22))
                .bookingNo(b.getBookingNumber())
                .carName(b.getCar().getModel())
                .status(ETransactionStatus.SUCCESSFUL)
                .wallet(carOwnerWallet)
                .build();
        transactionRepository.save(transactionCarOwner);
    }


    //refund ALL deposit
    //when bookingStatus = WAITING_CONFIRMED
    // car owner refuse that booking
    public void refundAllDeposit(Booking b){
        //pay 100% deposit to customer
        Wallet customerWallet = transferFromSystemToUser(b.getAccount().getId(), b.getDeposit());
        // save as new transaction
        Transaction transaction = Transaction.builder()
                .type(ETransactionType.REFUND_DEPOSIT)
                .amount(b.getDeposit())
                .bookingNo(b.getBookingNumber())
                .carName(b.getCar().getModel())
                .status(ETransactionStatus.SUCCESSFUL)
                .wallet(customerWallet)
                .build();
        transactionRepository.save(transaction);
    }

    // OFFSET FINAL PAYMENT when booking is COMPLETED
    public void offsetFinalPayment(Booking b){

        // count total
        long totalPayment = calculateTotalPayment(b);

        Wallet customerWallet ;
        Wallet carOnwerWallet;
        // When deposit is less than total, system will deduct money from user’s wallet
        if(b.getDeposit() < totalPayment){
             customerWallet = transferFromUserToSystem(b.getAccount().getId(), totalPayment - b.getDeposit());
        }
        //If the deposit amount is more than the total amount, system will return money to user’s wallet
        else{
             customerWallet = transferFromSystemToUser(b.getAccount().getId(), b.getDeposit() - totalPayment);
        }
        carOnwerWallet = transferFromSystemToUser(b.getCar().getAccount().getId(),(long)(totalPayment*0.92 ));
        //Create transaction for customer wallet
        Transaction transactionCustomer = Transaction.builder()
                .type(ETransactionType.OFFSET_FINAL_PAYMENT)
                .amount(b.getDeposit())
                .bookingNo(b.getBookingNumber())
                .carName(b.getCar().getModel())
                .status(ETransactionStatus.SUCCESSFUL)
                .wallet(customerWallet)
                .build();
        transactionRepository.save(transactionCustomer);
        //Create transaction for car owner wallet
        Transaction transactionCarOwner = Transaction.builder()
                .type(ETransactionType.RECEIVE_DEPOSIT)
                .amount(b.getDeposit())
                .bookingNo(b.getBookingNumber())
                .carName(b.getCar().getModel())
                .status(ETransactionStatus.SUCCESSFUL)
                .wallet(carOnwerWallet)
                .build();
        transactionRepository.save(transactionCarOwner);
    }

    private long calculateTotalPayment(Booking b){
        long minutes = Duration.between(b.getPickUpTime(), b.getDropOffTime()).toMinutes();
        long days = (long) Math.ceil(minutes / (24.0 * 60)); // Convert minutes to full days.
        return  b.getBasePrice() * days;
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
