package com.mp.karental.entity;

import com.mp.karental.constant.ETransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Immutable
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

    String message; //save the transaction message, could be remove
}
