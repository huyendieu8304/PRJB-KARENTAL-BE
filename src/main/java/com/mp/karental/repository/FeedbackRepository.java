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
     *
     * @param carIds   A list of car IDs.
     * @param rating   The rating value to filter by.
     * @param pageable The pagination information.
     * @return A page of feedback filtered by car IDs and rating.
     */
    @Query("""
        SELECT f FROM Feedback f 
        WHERE f.booking.car.id IN :carIds 
        AND f.rating = :rating
        ORDER BY f.createAt DESC
    """)
    Page<Feedback> findByCarIdsAndRating(@Param("carIds") List<String> carIds,
                                         @Param("rating") int rating,
                                         Pageable pageable);

    /**
     * Counts the number of feedback entries for each rating (1-5) for a given set of cars.
     *
     * @param carIds A list of car IDs.
     * @return A list of objects where each element contains a rating value and its count.
     */
    @Query("""
        SELECT f.rating, COUNT(f.id) 
        FROM Feedback f 
        WHERE f.booking.car.id IN :carIds 
        GROUP BY f.rating
    """)
    List<Object[]> countFeedbackByRating(@Param("carIds") List<String> carIds);

    /**
     * Calculates the average rating for all feedback associated with the given car IDs.
     *
     * @param carIds A list of car IDs.
     * @return The average rating, or null if no feedback exists.
     */
    @Query(value = """
        SELECT AVG(f.rating) 
        FROM feedback f
        JOIN booking b ON f.booking_number = b.booking_number
        WHERE b.car_id IN (:carIds)
    """, nativeQuery = true)
    Double calculateAverageRatingByOwner(@Param("carIds") List<String> carIds);

    /**
     * Calculates the average rating for each individual car in the given list.
     *
     * @param carIds A list of car IDs.
     * @return A list of objects where each element contains a car ID and its average rating.
     */
    @Query(value = """
        SELECT b.car_id, AVG(f.rating) 
        FROM feedback f
        JOIN booking b ON f.booking_number = b.booking_number
        WHERE b.car_id IN (:carIds)
        GROUP BY b.car_id
    """, nativeQuery = true)
    List<Object[]> calculateAverageRatingByCar(@Param("carIds") List<String> carIds);

    /**
     * Calculates the average rating for a specific car.
     *
     * @param carId The unique identifier of the car.
     * @return The average rating for the car, or null if no feedback exists.
     */
    @Query(value = """
        SELECT AVG(f.rating) 
        FROM feedback f
        JOIN booking b ON f.booking_number = b.booking_number
        WHERE b.car_id = :carId
    """, nativeQuery = true)
    Double calculateAverageRatingByCar(@Param("carId") String carId);
}
