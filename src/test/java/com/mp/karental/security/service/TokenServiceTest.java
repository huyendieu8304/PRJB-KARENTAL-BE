package com.mp.karental.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is a class used to test TokenService, service used to check is the token invalidated and invalidate token
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void invalidateAccessToken_ShouldSetKeyAndExpireAt() {
        // Arrange
        String token = "test-access-token";
        Instant expireAt = Instant.now().plusSeconds(3600);

        // Act
        tokenService.invalidateAccessToken(token, expireAt);

        // Assert
        verify(redisTemplate.opsForValue()).set("accessTk:" + token, "");
        verify(redisTemplate).expireAt("accessTk:" + token, expireAt);
    }

    @Test
    void isAccessTokenInvalidated_ShouldReturnTrue_WhenKeyExists() {
        // Arrange
        String token = "test-access-token";
        when(redisTemplate.hasKey("accessTk:" + token)).thenReturn(true);

        // Act
        boolean result = tokenService.isAccessTokenInvalidated(token);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey("accessTk:" + token);
    }

    @Test
    void isAccessTokenInvalidated_ShouldReturnFalse_WhenKeyNotExists() {
        // Arrange
        String token = "test-access-token";
        when(redisTemplate.hasKey("accessTk:" + token)).thenReturn(false);

        // Act
        boolean result = tokenService.isAccessTokenInvalidated(token);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey("accessTk:" + token);
    }

    @Test
    void invalidateRefreshToken_ShouldSetKeyAndExpireAt() {
        // Arrange
        String token = "test-refresh-token";
        Instant expireAt = Instant.now().plusSeconds(3600);

        // Act
        tokenService.invalidateRefreshToken(token, expireAt);

        // Assert
        verify(redisTemplate.opsForValue()).set("refreshTk:" + token, "");
        verify(redisTemplate).expireAt("refreshTk:" + token, expireAt);
    }

    @Test
    void isRefreshTokenInvalidated_ShouldReturnTrue_WhenKeyExists() {
        // Arrange
        String token = "test-refresh-token";
        when(redisTemplate.hasKey("refreshTk:" + token)).thenReturn(true);

        // Act
        boolean result = tokenService.isRefreshTokenInvalidated(token);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey("refreshTk:" + token);
    }

    @Test
    void isRefreshTokenInvalidated_ShouldReturnFalse_WhenKeyNotExists() {
        // Arrange
        String token = "test-refresh-token";
        when(redisTemplate.hasKey("refreshTk:" + token)).thenReturn(false);

        // Act
        boolean result = tokenService.isRefreshTokenInvalidated(token);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey("refreshTk:" + token);
    }
}