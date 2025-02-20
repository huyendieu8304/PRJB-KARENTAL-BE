package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CollectionType;

import java.util.List;

/**
 * Represents a car entity in the system.
 * <p>
 * This entity maps to a database table for storing car information,
 * </p>
 *
 * @author QuangPM20
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
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(name = "license_plate", nullable = false, unique = true)
    String licensePlate;

    @Column(nullable = false)
    String brand;

    @Column(nullable = false)
    String model;

    @Column(nullable = false)
    String status;

    @Column(nullable = false)
    String color;

    @Column(name = "number_of_seats",nullable = false)
    int numberOfSeats;

    @Column(name = "production_year",nullable = false)
    int productionYear;
    @Column(nullable = false)
    float mileage;
    @Column(name = "fuel_consumption")
    float fuelConsumption;
    @Column(name = "base_price",nullable = false)
    int basePrice;
    @Column(nullable = false)
    int deposit;
    @Column(name = "reservation_price",nullable = false)
    int reservationPrice;

    @Column(nullable = false)
    String address;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "additional_function")
    String additionalFunction;

    @Column(name = "term_of_use")
    String termOfUse;

    @Column(nullable = false)
    boolean isAutomatic = true;
    @Column(nullable = false)
    boolean isGasoline = true;

    //documents
    @Column(name = "registration_paper_uri",columnDefinition = "Text", nullable = false)
    String registrationPaperUri;
    @Column(nullable = false)
    boolean registrationPaperUriIsVerified;
    @Column(name = "certificate_of_inspection_uri",columnDefinition = "Text", nullable = false)
    String certificateOfInspectionUri;
    @Column(nullable = false)
    boolean certificateOfInspectionUriIsVerified;
    @Column(name = "insurance_uri",columnDefinition = "Text", nullable = false)
    String insuranceUri;
    @Column(nullable = false)
    boolean insuranceUriIsVerified;

    //car image
    @Column(name = "car_image_front",columnDefinition = "Text", nullable = false)
    String carImageFront;
    @Column(name = "car_image_back",columnDefinition = "Text", nullable = false)
    String carImageBack;
    @Column(name = "car_image_left",columnDefinition = "Text", nullable = false)
    String carImageLeft;
    @Column(name = "car_image_right",columnDefinition = "Text", nullable = false)
    String carImageRight;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    UserProfile accountId;

}
