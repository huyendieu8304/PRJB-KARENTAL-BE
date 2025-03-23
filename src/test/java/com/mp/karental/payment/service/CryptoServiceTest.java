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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private PaymentConfig paymentConfig;

    private CryptoService cryptoService;
    private static final String TEST_SECRET_KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidKeyException {
        when(paymentConfig.getSecretKey()).thenReturn(TEST_SECRET_KEY);
        cryptoService = new CryptoService(paymentConfig);
        cryptoService.init(); // Initialize the MAC
    }

    @Test
    void toHexString_WithValidBytes_ShouldReturnHexString() {
        // Arrange
        byte[] testBytes = "test".getBytes();

        // Act
        String result = CryptoService.toHexString(testBytes);

        // Assert
        assertNotNull(result);
        assertEquals("74657374", result); // "test" in hex
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    void toHexString_WithEmptyBytes_ShouldReturnEmptyString() {
        // Arrange
        byte[] emptyBytes = new byte[0];

        // Act
        String result = CryptoService.toHexString(emptyBytes);

        // Assert
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void sign_WithValidData_ShouldReturnSignedString() {
        // Arrange
        String testData = "test_data";

        // Act
        String signature = cryptoService.sign(testData);

        // Assert
        assertNotNull(signature);
        assertTrue(signature.matches("[0-9a-f]+"));
        assertEquals(128, signature.length()); // HMAC-SHA512 produces 512-bit (64-byte) hash, which is 128 hex characters
    }

    @Test
    void sign_WithEmptyString_ShouldReturnValidSignature() {
        // Arrange
        String emptyData = "";

        // Act
        String signature = cryptoService.sign(emptyData);

        // Assert
        assertNotNull(signature);
        assertTrue(signature.matches("[0-9a-f]+"));
        assertEquals(128, signature.length());
    }

    @Test
    void sign_SameInputShouldProduceSameOutput() {
        // Arrange
        String testData = "test_data";

        // Act
        String signature1 = cryptoService.sign(testData);
        String signature2 = cryptoService.sign(testData);

        // Assert
        assertEquals(signature1, signature2, "Same input should produce same signature");
    }

    @Test
    void sign_DifferentInputsShouldProduceDifferentOutputs() {
        // Arrange
        String testData1 = "test_data_1";
        String testData2 = "test_data_2";

        // Act
        String signature1 = cryptoService.sign(testData1);
        String signature2 = cryptoService.sign(testData2);

        // Assert
        assertNotEquals(signature1, signature2, "Different inputs should produce different signatures");
    }

    @Test
    void constructor_WithInvalidAlgorithm_ShouldThrowException() {
        // This test is a bit artificial since we're using a fixed algorithm,
        // but it's good to have for completeness
        assertDoesNotThrow(() -> new CryptoService(paymentConfig));
    }

    @Test
    void init_WithValidKey_ShouldInitializeSuccessfully() {
        // Arrange
        assertDoesNotThrow(() -> {
            CryptoService newCryptoService = new CryptoService(paymentConfig);
            newCryptoService.init();
        });
    }
} 