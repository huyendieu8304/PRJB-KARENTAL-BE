package com.mp.karental.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mp.karental.constant.ERole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING) //to save the name as String in db
    private ERole name;

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    private List<Account> accounts;
}
