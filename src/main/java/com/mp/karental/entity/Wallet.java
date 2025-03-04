package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
    @MapsId
    Account account;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    long balance;

}
