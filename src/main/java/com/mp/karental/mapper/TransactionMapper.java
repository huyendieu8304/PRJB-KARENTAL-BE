package com.mp.karental.mapper;

import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.TransactionResponse;
import com.mp.karental.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransaction(TransactionRequest transactionRequest);
    TransactionResponse toTransactionResponse(Transaction transaction);



}
