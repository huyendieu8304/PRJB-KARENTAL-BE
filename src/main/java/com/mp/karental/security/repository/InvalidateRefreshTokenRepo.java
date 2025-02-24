package com.mp.karental.security.repository;

import com.mp.karental.security.entity.InvalidateRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvalidateRefreshTokenRepo extends JpaRepository<InvalidateRefreshToken, Long> {
    Optional<InvalidateRefreshToken> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
