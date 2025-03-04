package com.mp.karental.repository;

import com.mp.karental.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * Repository interface for performing CRUD operations on Wallet entities.
 * <p>
 * This interface extends {@link JpaRepository}, providing standard methods for data access,
 * such as saving, deleting, and finding entities.
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 * @see JpaRepository
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    Wallet findByAccountId(String accountId);
}
