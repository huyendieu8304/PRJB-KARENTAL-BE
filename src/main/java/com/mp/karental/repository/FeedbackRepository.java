package com.mp.karental.repository;

import com.mp.karental.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Feedback entity.
 * Provides database operations related to feedback.
 *
 * @author AnhHP9
 * @version 1.0
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    /**
     * Retrieves all feedback associated with a specific booking.
     *
     * @param bookingNumber The unique identifier of the booking.
     * @return A list of feedback related to the given booking.
     */
    @Query("SELECT f FROM Feedback f WHERE f.booking.bookingNumber = :bookingNumber")
    List<Feedback> findByBookingNumber(@Param("bookingNumber") String bookingNumber);

    /**
     * Checks if a feedback entry exists in the database by its ID.
     *
     * @param id The ID of the feedback.
     * @return true if a feedback with the given ID exists, otherwise false.
     */
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.id = :id")
    boolean existsById(@Param("id") String id);

    /**
     * Retrieves all feedback associated with a specific car.
     *
     * @param carId The unique identifier of the car.
     * @return A list of feedback related to the given car.
     */
    @Query("SELECT f FROM Feedback f WHERE f.booking.car.id = :carId")
    List<Feedback> findByCarId(@Param("carId") String carId);

}
