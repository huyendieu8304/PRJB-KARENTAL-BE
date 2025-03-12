package com.mp.karental.repository;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on Car entities.
 * <p>
 * This interface extends {@link JpaRepository}, providing standard methods for data access,
 * such as saving, deleting, and finding entities.
 * </p>
 *
 * @author QuangPM20
 *
 * @version 1.0
 * @see JpaRepository
 */

@Repository
public interface CarRepository extends JpaRepository<Car, String> {
    Optional<Car> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    @Query("SELECT c FROM Car c WHERE c.account.id = :accountId")
    Page<Car> findByAccountId(String accountId, Pageable pageable);

    @Query("""
    SELECT c FROM Car c 
    WHERE c.status = :status 
    AND LOWER(REPLACE(CONCAT(c.cityProvince, ' ', c.district, ' ', c.ward), ',', '')) 
        LIKE LOWER(REPLACE(CONCAT('%', :address, '%'), ',', ''))
""")
    Page<Car> findVerifiedCarsByAddress(@Param("status") ECarStatus status,
                                        @Param("address") String address,
                                        Pageable pageable);



}

