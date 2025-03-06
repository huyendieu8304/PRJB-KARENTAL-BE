package com.mp.karental.repository;

import com.mp.karental.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByWalletId(String walletId);

    @Query("Select t from Transaction t where t.createdAt between :from and :to and t.wallet.id = :accountId")
    List<Transaction> getTransactionsByDate(String accountId, LocalDateTime from, LocalDateTime to);

    List<Transaction> getTransactionsByWalletId(String walletId);
}
