package com.mp.karental.repository;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.car.id = :carId AND b.account.id = :accountId AND b.status IN :statuses")
    boolean existsByCarIdAndAccountIdAndBookingStatusIn(
            @Param("carId") String carId,
            @Param("accountId") String accountId,
            @Param("statuses") List<EBookingStatus> statuses
    );

    @Query("""
    SELECT COUNT(b) 
    FROM Booking b 
    WHERE b.car.id = :carId 
    AND b.status <> :cancelledStatus 
    AND (
        :startRange BETWEEN b.pickUpTime AND b.dropOffTime 
        OR :endRange BETWEEN b.pickUpTime AND b.dropOffTime
        OR (b.pickUpTime <= :startRange AND b.dropOffTime >= :endRange)
    )
""")
    long countActiveBookingsInTimeRange(@Param("carId") String carId,
                                        @Param("startRange") LocalDateTime startRange,
                                        @Param("endRange") LocalDateTime endRange,
                                        @Param("cancelledStatus") EBookingStatus cancelledStatus);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING_DEPOSIT' " +
            "AND b.paymentType = 'WALLET' " +
            "AND b.createdAt <= :expiredTime")
    List<Booking> findExpiredBookings(@Param("expiredTime") LocalDateTime expiredTime);
}