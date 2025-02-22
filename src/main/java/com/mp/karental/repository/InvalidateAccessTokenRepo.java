package com.mp.karental.repository;

import com.mp.karental.entity.InvalidateAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Repository
public interface InvalidateAccessTokenRepo extends JpaRepository<InvalidateAccessToken, Long> {
    Optional<InvalidateAccessToken> findByToken(String token);
    void deleteByExpiresAtBefore(Date now);
}
