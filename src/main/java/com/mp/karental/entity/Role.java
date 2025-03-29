package com.mp.karental.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mp.karental.constant.ERole;
import com.mp.karental.security.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Represents a role entity in the system.
 * <p>
 * This entity maps to a database table for storing role information,
 * including role id, role's name, and associated user account.
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
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING) //to save the name as String in db
    private ERole name;

    @PostPersist
    public void onPostPersist() {
        log.info("Account: {} - Successfully created Role with id: {}", SecurityUtil.getCurrentAccountId(), this.id);
    }

    @PreUpdate
    public void onPreUpdate() {
        log.info("Account: {} - Updating Role: {}", SecurityUtil.getCurrentAccountId(), this);
    }

    @PostUpdate
    public void onPostUpdate() {
        log.info("Account: {} - Updated Role: {}", SecurityUtil.getCurrentAccountId(), this);
    }

}
