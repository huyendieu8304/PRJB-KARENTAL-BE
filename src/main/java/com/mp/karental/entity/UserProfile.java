package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a user profile entity in the system.
 * <p>
 * This entity maps to a database table for storing user information,
 * including full name, date of birth, national id, phone number, address, driving licence and associated user account.
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
public class UserProfile {
    @Id
    String id;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId
    private Account account;

    String fullName;
    LocalDate dob;

    @Column(unique = true, columnDefinition = "VARCHAR(12)")
    String nationalId;

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(10)")
    String phoneNumber;

    String address;

    @Column(unique = true, columnDefinition = "VARCHAR(12)")
    String drivingLicense;

    @OneToMany(mappedBy = "accountId", cascade = CascadeType.ALL)
    List<Car> cars;

}
