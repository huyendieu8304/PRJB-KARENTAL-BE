package com.mp.karental.entity;

import com.mp.karental.security.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class Wallet {
    @Id
    String id;
    @OneToOne
    @JoinColumn(name = "id")
    @MapsId
    Account account;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    long balance;

    @PostPersist
    public void onPostPersist() {
        String accountId = SecurityUtil.getCurrentAccountId() == null ? "This user" : SecurityUtil.getCurrentAccountId();
        log.info("Account: {} - Successfully created UserProfile with id: {}", accountId, this.id);
    }

    @PreUpdate
    public void onPreUpdate() {
        log.info("Account: {} - Updating UserProfile: {}", SecurityUtil.getCurrentAccountId(), this);
    }

    @PostUpdate
    public void onPostUpdate() {
        log.info("Account: {} - Updated UserProfile: {}", SecurityUtil.getCurrentAccountId(), this);
    }
}
