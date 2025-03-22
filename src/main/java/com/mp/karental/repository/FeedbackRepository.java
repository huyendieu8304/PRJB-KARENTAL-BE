package com.mp.karental.repository;

import com.mp.karental.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Feedback entity.
 * <p>
 * This interface provides methods for querying feedback data,
 * including retrieving feedback by booking, car, and rating.
 * It extends {@link JpaRepository} to leverage basic CRUD operations.
 * </p>
 *
 * Author: AnhHP9
 * Version: 1.0
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    /**
     * Retrieves feedback for a specific booking.
     *
     * @param bookingNumber The unique identifier of the booking.
     * @return The feedback entity or empty if not found.
     */
    @Query("SELECT f FROM Feedback f WHERE f.booking.bookingNumber = :bookingNumber")
    Optional<Feedback> findByBookingNumber(@Param("bookingNumber") String bookingNumber);

    /**
     * Checks if a feedback entry exists in the database by its ID.
     *
     * @param id The ID of the feedback.
     * @return {@code true} if a feedback with the given ID exists, otherwise {@code false}.
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

    /**
     * Retrieves paginated feedback for multiple cars, ordered by creation date in descending order.
     * <p>
     * This method is useful for displaying feedback for a list of cars belonging to a specific owner.
     * </p>
     *
     * @param carIds   A list of car IDs.
     * @param pageable The pagination information.
     * @return A page of feedback related to the given car IDs.
     */
    @Query("""
        SELECT f FROM Feedback f 
        WHERE f.booking.car.id IN :carIds
        ORDER BY f.createAt DESC
    """)
    Page<Feedback> findByCarIds(@Param("carIds") List<String> carIds, Pageable pageable);

    /**
     * Retrieves paginated feedback for multiple cars with a specific rating.
     * <p>
     * This query joins the {@code Feedback}, {@code Booking}, and {@code Car} entities
     * to filter feedback by a list of car IDs and a specific rating.
     * </p>
     *
     * @param carIds   A list of car IDs.
     * @param rating   The rating value to filter by.
     * @param pageable The pagination information.
     * @return A page of feedback filtered by car IDs and rating.
     */
    @Query("""
        SELECT f FROM Feedback f 
        JOIN f.booking b
        JOIN b.car c
        WHERE c.id IN :carIds 
        AND f.rating = :rating
        ORDER BY f.createAt DESC
    """)
    Page<Feedback> findByCarIdsAndRating(@Param("carIds") List<String> carIds,
                                         @Param("rating") int rating,
                                         Pageable pageable);


    /**
     * Calculates the average rating for feedback associated with a list of cars.
     * <p>
     * This method retrieves the average rating from feedback records where the feedback is linked
     * to bookings that belong to the specified cars. If a rating filter (1-5) is provided,
     * only feedback with that rating is considered. If ratingFilter = 0, all ratings are included.
     * </p>
     *
     * @param carIds      The list of car IDs owned by the user.
     * @param ratingFilter The rating filter (1-5 for specific ratings, 0 for all ratings).
     * @return The average rating, or null if no feedback is found.
     */
    @Query(value = "SELECT AVG(f.rating) " +
            "FROM feedback f " +
            "JOIN booking b ON f.booking_number = b.booking_number " +
            "JOIN car c ON b.car_id = c.id " +
            "WHERE c.id IN (:carIds) " +
            "AND (:ratingFilter = 0 OR f.rating = :ratingFilter)",
            nativeQuery = true)
    Double calculateAverageRating(@Param("carIds") List<String> carIds,
                                  @Param("ratingFilter") int ratingFilter);

}
