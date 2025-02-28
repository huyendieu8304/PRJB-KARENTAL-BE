package com.mp.karental.entity;


import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a booking in the database
 * <p>
 * This entity maps to a database table for storing booking information
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
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "booking_number", unique = true, nullable = false)
    String bookingNumber;

    @PrePersist
    public void prePersist() {
        if(this.bookingNumber == null){
            this.bookingNumber = generateBookingNumber();
        }
    }

    @Enumerated(EnumType.STRING)
    EBookingStatus bookingStatus = EBookingStatus.PENDING_DEPOSIT;

    @Column(nullable = false)
    String pickUpLocation;

    @Column(nullable = false)
    LocalDateTime startDate;

    @Column(nullable = false)
    LocalDateTime endDate;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "car_id")
    Car car;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @Enumerated(EnumType.STRING)
    EPaymentType paymentType;

    //TODO: transaction_number cái này phải suy nghĩ thêm, túm lại là cái link từ booking đến việc thanh toán của user

    @Column(nullable = false)
    String renterFullName;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    String renterPhoneNumber;

    @Column(nullable = false, columnDefinition = "VARCHAR(12)")
    String renterNationalId;

    @Column(nullable = false)
    LocalDate renterDob;

    @Column(nullable = false)
    String renterEmail;

    @Column(nullable = false)
    String renterDrivingLicenseUri;

    @Column(nullable = false)
    String renterAddress;

    @Column(nullable = false)
    String driverFullName;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    String driverPhoneNumber;

    @Column(nullable = false, columnDefinition = "VARCHAR(12)")
    String driverNationalId;

    @Column(nullable = false)
    LocalDate driverDob;

    @Column(nullable = false)
    String driverEmail;

    @Column(nullable = false)
    String driverDrivingLicenseUri;

    @Column(nullable = false)
    String driverAddress;

    private String generateBookingNumber(){
        //get current date in form yyyyMMdd
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        //TODO: hỏi laik cái chỗ sequence này
        return date + "-" + UUID.randomUUID();
    }

}
