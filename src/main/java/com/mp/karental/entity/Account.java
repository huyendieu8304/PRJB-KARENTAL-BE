package com.mp.karental.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mp.karental.security.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user account entity in the system.
 * <p>
 * This entity maps to a database table for storing account information,
 * including email, password, role, status, timestamps, and associated user profile.
 * </p>
 *
 * @author DieuTTH4
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Slf4j
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    boolean isEmailVerified = false;

    @Column(name = "password", nullable = false)
    String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;

    @Column(nullable = false)
    boolean isActive;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    UserProfile profile;

    @PostPersist
    public void onPostPersist() {
        String accountId = SecurityUtil.getCurrentAccountId() == null ? "This user" : SecurityUtil.getCurrentAccountId();
        log.info("Account: {} - Successfully created Account with id: {}", accountId, this.id);
    }

    @PreUpdate
    public void onPreUpdate() {
        String accountId = SecurityUtil.getCurrentAccountId() == null ? "This user" : SecurityUtil.getCurrentAccountId();
        log.info("Account: {} - Updating Account: {}", accountId, this);
    }

    @PostUpdate
    public void onPostUpdate() {
        String accountId = SecurityUtil.getCurrentAccountId() == null ? "This user" : SecurityUtil.getCurrentAccountId();
        log.info("Account: {} - Updated Account: {}", accountId, this);
    }
}
