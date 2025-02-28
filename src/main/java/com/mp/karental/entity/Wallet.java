package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
/**
 * Represents a wallet of user in the database
 * <p>
 * This entity maps to a database table for storing user's wallet information
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
public class Wallet {
    @Id
    String id;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId
    Account account;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    long balance;

}
