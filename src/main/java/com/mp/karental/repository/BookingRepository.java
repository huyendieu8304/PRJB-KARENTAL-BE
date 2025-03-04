package com.mp.karental.repository;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 * Repository interface for performing CRUD operations on Booking entities.
 * <p>
 * This interface extends {@link JpaRepository}, providing standard methods for data access,
 * such as saving, deleting, and finding entities.
 * </p>
 *
 * @author AnhHP9
 *
 * @version 1.0
 * @see JpaRepository
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = 'COMPLETED' AND b.car.id = :carId")
    long countCompletedBookingsByCar(@Param("carId") String carId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.car.id = :carId AND b.account.id = :accountId AND b.car.status = 'BOOKED'")
    boolean isCarBookedByAccount(@Param("carId") String carId, @Param("accountId") String accountId);


}