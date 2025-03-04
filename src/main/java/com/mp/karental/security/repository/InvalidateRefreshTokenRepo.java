package com.mp.karental.security.repository;

import com.mp.karental.security.entity.InvalidateRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
/**
 * Repository interface for performing CRUD operations on InvalidateRefreshToken entities.
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
@Repository
public interface InvalidateRefreshTokenRepo extends JpaRepository<InvalidateRefreshToken, Long> {
    Optional<InvalidateRefreshToken> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
