package com.mp.karental.repository;

import com.mp.karental.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Optional<Feedback> findByBooking_BookingNumber(String bookingNumber);

    @Query("SELECT f FROM Feedback f WHERE f.booking.car.id = :carId")
    List<Feedback> findByCarId(@Param("carId") String carId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.booking.bookingNumber = :bookingNumber")
    Double findAverageRatingByBookingNumber(@Param("bookingNumber") String bookingNumber);

    @Query("""
        SELECT COALESCE(AVG(f.rating), 0) 
        FROM Feedback f 
        WHERE f.booking.car.id = :carId
    """)
    Double findAverageRatingByCarId(@Param("carId") String carId);

}
