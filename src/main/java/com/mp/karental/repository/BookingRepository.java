package com.mp.karental.repository;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.car.id = :carId " +
            "AND b.dropOffTime > CURRENT_TIMESTAMP " +
            "AND b.status NOT IN :excludedStatuses")
    boolean hasActiveBooking(@Param("carId") String carId,
                             @Param("excludedStatuses") List<EBookingStatus> excludedStatuses);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.car.id = :carId
    AND (
        (:startRange BETWEEN b.pickUpTime AND b.dropOffTime)
        OR (:endRange BETWEEN b.pickUpTime AND b.dropOffTime)
        OR (b.pickUpTime >= :startRange AND b.dropOffTime <= :endRange)
    )
""")
    List<Booking> findActiveBookingsByCarIdAndTimeRange(
            @Param("carId") String carId,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId AND b.status = :status " +
            "AND ((b.pickUpTime BETWEEN :pickUpTime AND :dropOffTime) OR " +
            "(b.dropOffTime BETWEEN :pickUpTime AND :dropOffTime) OR " +
            "(:pickUpTime BETWEEN b.pickUpTime AND b.dropOffTime) OR " +
            "(:dropOffTime BETWEEN b.pickUpTime AND b.dropOffTime))")
    List<Booking> findByCarIdAndStatusAndTimeOverlap(
            @Param("carId") String carId,
            @Param("status") EBookingStatus status,
            @Param("pickUpTime") LocalDateTime pickUpTime,
            @Param("dropOffTime") LocalDateTime dropOffTime
    );

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId AND b.status = :status")
    List<Booking> findByCarIdAndStatus(@Param("carId") String carId, @Param("status") EBookingStatus status);


    @Query("""
    SELECT b FROM Booking b
    JOIN FETCH b.car c
    WHERE b.account.id = :accountId
""")
    Page<Booking> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    @Query("""
    SELECT b FROM Booking b
    JOIN FETCH b.car c
    WHERE b.account.id = :accountId
    AND (:status = 'ALL' OR b.status = :status)
""")
    Page<Booking> findByAccountIdAndStatus(@Param("accountId") String accountId,
                                           @Param("status") EBookingStatus status,
                                           Pageable pageable);

    @Query("""
    SELECT COUNT(b) FROM Booking b
    WHERE b.car.account.id = :ownerId
    AND (:statuses IS NULL OR b.status IN :statuses)
""")
    int countOngoingBookingsByCar(
            @Param("ownerId") String ownerId,
            @Param("statuses") List<EBookingStatus> statuses
    );

    @Query("""
    SELECT COUNT(b) FROM Booking b
    WHERE b.car.account.id = :ownerId
    AND b.status = :status
""")
    int countBookingsByOwnerAndStatus(@Param("ownerId") String ownerId,
                                      @Param("status") EBookingStatus status);


    @Query("""
    SELECT b FROM Booking b
    JOIN b.car c
    WHERE c.account.id = :ownerId
    AND b.status <> :excludedStatus
""")
    Page<Booking> findBookingsByCarOwnerId(
            @Param("ownerId") String ownerId,
            @Param("excludedStatus") EBookingStatus excludedStatus,
            Pageable pageable
    );

    @Query("""
    SELECT b FROM Booking b
    JOIN b.car c
    WHERE c.account.id = :ownerId
    AND (:status IS NULL OR b.status = :status)
    AND b.status <> :excludedStatus
""")
    Page<Booking> findBookingsByCarOwnerIdAndStatus(
            @Param("ownerId") String ownerId,
            @Param("status") EBookingStatus status,
            @Param("excludedStatus") EBookingStatus excludedStatus,
            Pageable pageable
    );

    Booking findBookingByBookingNumber(String bookingNumber);


    @Query("""
    SELECT b FROM Booking b
    JOIN b.car c
    WHERE b.bookingNumber = :bookingNumber
    AND c.account.id = :ownerId
""")
    Booking findBookingByBookingNumberAndOwnerId(
            @Param("bookingNumber") String bookingNumber,
            @Param("ownerId") String ownerId
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.status = :status")
    Page<Booking> findBookingsByStatus(@Param("status") EBookingStatus status,
                                               Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "ORDER BY " +
            "   CASE " +
            "       WHEN b.status = 'PENDING_DEPOSIT' THEN 1 " +
            "       WHEN b.status = 'PENDING_PAYMENT' THEN 2 " +
            "       WHEN b.status = 'WAITING_CONFIRMED_RETURN_CAR' THEN 3 " +
            "       ELSE 4 END")
    Page<Booking> findAllBookings(Pageable pageable);

//    @Query("SELECT b FROM Booking b " +
//            "ORDER BY CASE WHEN :sort IS NULL AND b.status = :pendingStatus THEN 0 ELSE 1 END")
//    Page<Booking> findWhenStatusNullBookings(@Param("pendingStatus") EBookingStatus pendingStatus,
//                                             @Param("sort") String sort,
//                                             Pageable pageable);

    Optional<Booking> findByBookingNumber(String bookingNumber);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.pickUpTime <= :currentTime")
    List<Booking> findOverduePickups(@Param("status") EBookingStatus status,@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.dropOffTime <= :currentTime")
    List<Booking> findOverdueDropOffs(@Param("status") EBookingStatus status,@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus WHERE b.status = :oldStatus AND b.pickUpTime <= :currentTime")
    int bulkUpdateWaitingConfirmedStatus(
            @Param("newStatus") EBookingStatus newStatus,
            @Param("oldStatus") EBookingStatus oldStatus,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus WHERE b.status = :oldStatus AND b.dropOffTime <= :currentTime")
    int bulkUpdateWaitingConfirmedReturnCarStatus(
            @Param("newStatus") EBookingStatus newStatus,
            @Param("oldStatus") EBookingStatus oldStatus,
            @Param("currentTime") LocalDateTime currentTime
    );
}
