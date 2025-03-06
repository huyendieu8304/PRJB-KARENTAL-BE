package com.mp.karental.entity;

import com.mp.karental.constant.ETransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Represents a transaction of user using system's wallet in the database
 * <p>
 * This entity maps to a database table for storing money transaction information
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Immutable //can not edit after create
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @CreationTimestamp
    LocalDateTime createdAt;

    //one directional mapping is enough
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    Wallet wallet;

    @Enumerated(EnumType.STRING)
    ETransactionType type;

    @Column(nullable = false)
    long amount;
    String bookingNo;
    String carName;
    String message; //save the transaction message, could be remove

    //One booking would have different transaction type
    @ManyToOne
    @JoinColumn(name = "booking_number")
    Booking bookingNumber;

}
