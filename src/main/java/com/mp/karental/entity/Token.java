package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(columnDefinition = "TEXT")
    String accessToken;

    @Column(columnDefinition = "TEXT")
    String refreshToken;

    @ManyToOne
    @JoinColumn(name = "account_id") //the column contain foreign key to Account
    Account account;
}
