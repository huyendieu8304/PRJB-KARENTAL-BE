package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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


}
