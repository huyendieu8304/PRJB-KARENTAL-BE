package com.mp.karental.security.repository;

import com.mp.karental.security.entity.InvalidateAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
/**
 * Repository interface for performing CRUD operations on InvalidateAccessToken entities.
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
public interface InvalidateAccessTokenRepo extends JpaRepository<InvalidateAccessToken, Long> {
    Optional<InvalidateAccessToken> findByToken(String token);
    void deleteByExpiresAtBefore(Date now);
}
