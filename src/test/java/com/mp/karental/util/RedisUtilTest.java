package com.mp.karental.util;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is a class used to test RedisUtil, contains method to work with redis
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisUtilTest {
    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    RedisUtil redisUtil;

    private static final String BOOKING_SEQUENCE_KEY = "booking-sequence";
    private static final String PENDING_DEPOSIT_BOOKING_KEY = "booking:";
    private static final String VERIFY_EMAIL_TOKEN_PREFIX = "verify-email-tk:";
    private static final String FORGOT_PASSWORD_TOKEN_PREFIX = "forgot-password-tk:";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateBookingNumber_WhenSequenceIsFirst_ShouldSetExpireAt() {
        // Arrange
        when(valueOperations.increment(BOOKING_SEQUENCE_KEY, 1)).thenReturn(1L);

        // Act
        String bookingNumber = redisUtil.generateBookingNumber();

        // Assert
        String expectedDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        assertTrue(bookingNumber.matches(expectedDate + "-\\d{8}"));

        // Check  expireAt called when sequence is first sequence
        verify(redisTemplate, times(1)).expireAt(eq(BOOKING_SEQUENCE_KEY), any(Date.class));
    }

    @Test
    void generateBookingNumber_WhenSequenceIsNotFirst_ShouldNotSetExpireAt() {
        // Arrange
        when(valueOperations.increment(BOOKING_SEQUENCE_KEY, 1)).thenReturn(2L);

        // Act
        String bookingNumber = redisUtil.generateBookingNumber();

        // Assert
        String expectedDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        assertTrue(bookingNumber.matches(expectedDate + "-\\d{8}"));

        // Check  expireAt should not be called when sequence is first sequence
        verify(redisTemplate, never()).expireAt(eq(BOOKING_SEQUENCE_KEY), any(Date.class));
    }

    @Test
    void generateBookingNumber_ShouldFormatBookingNumberCorrectly() {
        // Arrange
        long sequence = 123;
        when(valueOperations.increment(BOOKING_SEQUENCE_KEY, 1)).thenReturn(sequence);

        // Act
        String bookingNumber = redisUtil.generateBookingNumber();

        // Assert
        String expectedDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String expectedNumber = String.format("%08d", sequence);
        assertEquals(expectedDate + "-" + expectedNumber, bookingNumber);
    }

    @Test
    void generateVerifyEmailToken_ShouldGenerateTokenAndStoreInRedis() {
        // Arrange
        String accountId = "test-account-id";

        // Act
        String token = redisUtil.generateVerifyEmailToken(accountId);

        // Assert
        assertNotNull(token);
        assertDoesNotThrow(() -> UUID.fromString(token)); // Check token is valid UUID

        String expectedKey = VERIFY_EMAIL_TOKEN_PREFIX + token;
        // Check Redis called with correct key, value and expire time
        verify(valueOperations).set(expectedKey, accountId, 30, TimeUnit.MINUTES);
    }

    @Test
    void getValueOfVerifyEmailToken_ShouldReturnAccountId_WhenTokenIsValid() {
        // Arrange
        String token = "test-token";
        String expectedAccountId = "test-account-id";
        String key = VERIFY_EMAIL_TOKEN_PREFIX + token;

        // mock
        when(valueOperations.getAndDelete(key)).thenReturn(expectedAccountId);

        // Act
        String accountId = redisUtil.getValueOfVerifyEmailToken(token);

        // Assert
        assertNotNull(accountId);
        assertEquals(expectedAccountId, accountId);

        // Check redis called with correct key and execute getAndDelete
        verify(valueOperations).getAndDelete(key);
    }

    @Test
    void getValueOfVerifyEmailToken_ShouldReturnNull_WhenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";
        String key = VERIFY_EMAIL_TOKEN_PREFIX + token;

        // mock
        when(valueOperations.getAndDelete(key)).thenReturn(null);

        // Act
        String accountId = redisUtil.getValueOfVerifyEmailToken(token);

        // Assert
        assertNull(accountId);

        // Check redis called with correct key and execute getAndDelete
        verify(valueOperations).getAndDelete(key);
    }

    @Test
    void generateForgotPasswordToken_ShouldReturnToken_WhenAccountIdIsValid() {
        // Arrange
        String accountId = "test-account-id";

        // Act
        String token = redisUtil.generateForgotPasswordToken(accountId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        String expectedKey = FORGOT_PASSWORD_TOKEN_PREFIX + token;

        // Check redis called with correct key and exprire time is 24 hours
        verify(valueOperations).set(expectedKey, accountId, 24, TimeUnit.HOURS);
    }

    @Test
    void getValueOfForgotPasswordToken_ShouldReturnAccountId_WhenTokenExists() {
        // Arrange
        String token = "test-token";
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        String expectedAccountId = "test-account-id";

        when(valueOperations.get(key)).thenReturn(expectedAccountId);

        // Act
        String accountId = redisUtil.getValueOfForgotPasswordToken(token);

        // Assert
        assertEquals(expectedAccountId, accountId);
        verify(valueOperations).get(key);
    }

    @Test
    void getValueOfForgotPasswordToken_ShouldReturnNull_WhenTokenDoesNotExist() {
        // Arrange
        String token = "invalid-token";
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;

        when(valueOperations.get(key)).thenReturn(null);

        // Act
        String accountId = redisUtil.getValueOfForgotPasswordToken(token);

        // Assert
        assertNull(accountId);
        verify(valueOperations).get(key);
    }

    @Test
    void deleteForgotPasswordToken_ShouldDeleteToken_WhenTokenExists() {
        // Arrange
        String token = "test-token";
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;

        // Act
        redisUtil.deleteForgotPasswordToken(token);

        // Assert
        verify(redisTemplate).delete(key);
    }

    @Test
    void cachePendingDepositBooking_ShouldSetKeyWithTTL_WhenBookingNumberIsProvided() {
        // Arrange
        String bookingNumber = "12345";
        String key = PENDING_DEPOSIT_BOOKING_KEY + bookingNumber;

        // Act
        redisUtil.cachePendingDepositBooking(bookingNumber);

        // Assert
        verify(redisTemplate.opsForValue()).set(eq(key), eq(key), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    void removeCachePendingDepositBooking_ShouldDeleteKey_WhenBookingNumberIsProvided() {
        // Arrange
        String bookingNumber = "12345";
        String key = PENDING_DEPOSIT_BOOKING_KEY + bookingNumber;

        // Act
        redisUtil.removeCachePendingDepositBooking(bookingNumber);

        // Assert
        verify(redisTemplate).delete(key);
    }

}