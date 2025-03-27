package com.mp.karental.payment.service;

import com.mp.karental.exception.AppException;
import com.mp.karental.payment.configuration.PaymentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    private static final String TEST_SECRET_KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

    @Mock
    private PaymentConfig paymentConfig;

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() throws InvalidKeyException, NoSuchAlgorithmException {
        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(paymentConfig.getSecretKey()).thenReturn(TEST_SECRET_KEY);
        cryptoService = new CryptoService(paymentConfig);
        cryptoService.init();
    }

    @Test
    void sign_WithValidInput_ShouldReturnHash() {
        // Arrange
        String input = "test_input";

        // Act
        String result = cryptoService.sign(input);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void sign_WithEmptyInput_ShouldReturnValidHash() {
        // Arrange
        String input = "";

        // Act
        String result = cryptoService.sign(input);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void sign_WithNullInput_ShouldThrowAppException() {
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> 
            cryptoService.sign(null));
        // Note the extra space after "wrong." in the actual message
        assertEquals("Your information is wrong. ", exception.getMessage());
    }

    @Test
    void sign_WithSpecialCharacters_ShouldReturnValidHash() {
        // Arrange
        String input = "test!@#$%^&*()_+{}[]|\\:;\"'<>,.?/~`";

        // Act
        String result = cryptoService.sign(input);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void init_WithValidKey_ShouldInitializeSuccessfully() throws InvalidKeyException, NoSuchAlgorithmException {
        // Arrange
        CryptoService newCryptoService = new CryptoService(paymentConfig);

        // Act & Assert
        assertDoesNotThrow(newCryptoService::init);
    }



    @Test
    void init_WithNullSecretKey_ShouldThrowException() throws NoSuchAlgorithmException {
        // Arrange
        when(paymentConfig.getSecretKey()).thenReturn(null);
        CryptoService nullKeyCryptoService = new CryptoService(paymentConfig);

        // Act & Assert
        assertThrows(NullPointerException.class, nullKeyCryptoService::init);
    }

    @Test
    void sign_WithConsecutiveCalls_ShouldReturnSameHashForSameInput() {
        // Arrange
        String input = "test_input";

        // Act
        String result1 = cryptoService.sign(input);
        String result2 = cryptoService.sign(input);

        // Assert
        assertEquals(result1, result2, "Hash should be consistent for same input");
    }

    @Test
    void sign_WithDifferentInputs_ShouldReturnDifferentHashes() {
        // Arrange
        String input1 = "test_input_1";
        String input2 = "test_input_2";

        // Act
        String result1 = cryptoService.sign(input1);
        String result2 = cryptoService.sign(input2);

        // Assert
        assertNotEquals(result1, result2, "Different inputs should produce different hashes");
    }
} 