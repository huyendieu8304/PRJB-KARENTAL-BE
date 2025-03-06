package com.mp.karental.repository;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Repository interface for performing CRUD operations on Booking entities.
 * <p>
 * This interface extends {@link JpaRepository}, providing standard methods for data access,
 * such as saving, deleting, and finding entities.
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 * @see JpaRepository
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'COMPLETED' AND b.car.id = :carId")
    long countCompletedBookingsByCar(@Param("carId") String carId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.car.id = :carId AND b.account.id = :accountId AND b.status = 'COMPLETED'")
    boolean isCarBookedByAccount(@Param("carId") String carId, @Param("accountId") String accountId);

    @Query("""
    SELECT COUNT(b) > 0 
    FROM Booking b 
    WHERE b.car.id = :carId 
    AND (
        b.bookingStatus = :cancelledStatus 
        OR (
            :startRange BETWEEN b.startDate AND b.endDate 
            OR :endRange BETWEEN b.startDate AND b.endDate
            OR (b.startDate <= :startRange AND b.endDate >= :endRange)
        )
    )
""")
    boolean isCarBookedInTimeRange(@Param("carId") String carId,
                                   @Param("startRange") LocalDateTime startRange,
                                   @Param("endRange") LocalDateTime endRange,
                                   @Param("cancelledStatus") EBookingStatus cancelledStatus);

    boolean existsByCarId(String carId);



}