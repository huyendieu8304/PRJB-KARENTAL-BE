package com.mp.karental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a feedback entity for each booking in the system.
 * <p>
 * This entity maps to a database table for storing feedback booking information,
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
@Check(constraints = "rating >= 1 AND rating <= 5")
public class Feedback {
    @Id
    String id;

    @OneToOne
    @JoinColumn(name = "booking_number")
    @MapsId
    Booking booking;

    @Column(nullable = false)
    int rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;
}
