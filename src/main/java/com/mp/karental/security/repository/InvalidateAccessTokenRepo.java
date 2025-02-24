package com.mp.karental.security.repository;

import com.mp.karental.security.entity.InvalidateAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface InvalidateAccessTokenRepo extends JpaRepository<InvalidateAccessToken, Long> {
    Optional<InvalidateAccessToken> findByToken(String token);
    void deleteByExpiresAtBefore(Date now);
}
