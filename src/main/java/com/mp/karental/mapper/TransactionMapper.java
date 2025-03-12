package com.mp.karental.mapper;

import com.mp.karental.dto.request.transaction.TransactionRequest;
import com.mp.karental.dto.response.transaction.TransactionResponse;
import com.mp.karental.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransaction(TransactionRequest transactionRequest);
    TransactionResponse toTransactionResponse(Transaction transaction);



}
