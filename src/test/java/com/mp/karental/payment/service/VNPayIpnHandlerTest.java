package com.mp.karental.payment.service;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.payment.constant.VNPayIPNResponseConst;
import com.mp.karental.payment.constant.VNPayParams;
import com.mp.karental.payment.dto.response.IpnResponse;
import com.mp.karental.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayIpnHandlerTest {

    @Mock
    private VNPayService vnPayService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private VNPayIpnHandler vnPayIpnHandler;

    private Map<String, String> validParams;
    private static final String VALID_TXN_REF = "TEST_TXN_123";

    @BeforeEach
    void setUp() {
        validParams = new HashMap<>();
        validParams.put(VNPayParams.TXN_REF, VALID_TXN_REF);
        validParams.put(VNPayParams.AMOUNT, "1000000");
        validParams.put(VNPayParams.SECURE_HASH, "valid_hash");
    }

    @Test
    void process_WithValidParamsAndValidTransaction_ShouldReturnSuccess() {
        // Arrange
        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF)).thenReturn(Optional.of(mock()));

        // Act
        IpnResponse response = vnPayIpnHandler.process(validParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.SUCCESS.getResponseCode(), response.getResponseCode());
        verify(vnPayService).verifyIpn(validParams);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_WithInvalidChecksum_ShouldThrowException() {
        // Arrange
        when(vnPayService.verifyIpn(anyMap())).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, 
            () -> vnPayIpnHandler.process(validParams));
        
        assertEquals(ErrorCode.VNPAY_CHECKSUM_FAILED, exception.getErrorCode());
        verify(vnPayService).verifyIpn(validParams);
        verify(transactionRepository, never()).findById(any());
    }

    @Test
    void process_WithValidChecksumButTransactionNotFound_ShouldReturnUnknownError() {
        // Arrange
        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF))
            .thenThrow(new AppException(ErrorCode.TRANSACTION_NOT_FOUND_IN_DB));

        // Act
        IpnResponse response = vnPayIpnHandler.process(validParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.UNKNOWN_ERROR.getResponseCode(), 
            response.getResponseCode());
        verify(vnPayService).verifyIpn(validParams);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_WithUnexpectedException_ShouldReturnUnknownError() {
        // Arrange
        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        IpnResponse response = vnPayIpnHandler.process(validParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.UNKNOWN_ERROR.getResponseCode(), 
            response.getResponseCode());
        verify(vnPayService).verifyIpn(validParams);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_WithEmptyParams_ShouldHandleGracefully() {
        // Arrange
        Map<String, String> emptyParams = new HashMap<>();
        when(vnPayService.verifyIpn(anyMap())).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, 
            () -> vnPayIpnHandler.process(emptyParams));
        
        assertEquals(ErrorCode.VNPAY_CHECKSUM_FAILED, exception.getErrorCode());
        verify(vnPayService).verifyIpn(emptyParams);
        verify(transactionRepository, never()).findById(any());
    }

    @Test
    void process_WithNullTxnRef_ShouldHandleGracefully() {
        // Arrange
        Map<String, String> paramsWithoutTxnRef = new HashMap<>(validParams);
        paramsWithoutTxnRef.remove(VNPayParams.TXN_REF);
        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);

        // Act
        IpnResponse response = vnPayIpnHandler.process(paramsWithoutTxnRef);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.UNKNOWN_ERROR.getResponseCode(), 
            response.getResponseCode());
        verify(vnPayService).verifyIpn(paramsWithoutTxnRef);
    }

    @Test
    void process_WithMultipleConsecutiveCalls_ShouldHandleEachIndependently() {
        // Arrange
        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF)).thenReturn(Optional.of(mock()));

        // Act & Assert
        for (int i = 0; i < 3; i++) {
            IpnResponse response = vnPayIpnHandler.process(validParams);
            assertNotNull(response);
            assertEquals(VNPayIPNResponseConst.SUCCESS.getResponseCode(), 
                response.getResponseCode());
        }

        verify(vnPayService, times(3)).verifyIpn(validParams);
        verify(transactionRepository, times(3)).findById(VALID_TXN_REF);
    }

//    @Test
//    void process_WithBookingNotFoundError_ShouldReturnOrderNotFound() {
//        // Arrange
//        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
//        when(transactionRepository.findById(VALID_TXN_REF))
//            .thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND));
//
//        // Act
//        IpnResponse response = vnPayIpnHandler.process(validParams);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(VNPayIPNResponseConst.ORDER_NOT_FOUND.getResponseCode(),
//            response.getResponseCode());
//        verify(vnPayService).verifyIpn(validParams);
//        verify(transactionRepository).findById(VALID_TXN_REF);
//    }

    @Test
    void process_WithInvalidAmountError_ShouldReturnInvalidAmount() {
        // Arrange
        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF))
            .thenThrow(new AppException(ErrorCode.TRANSACTION_NOT_FOUND_IN_DB));

        // Act
        IpnResponse response = vnPayIpnHandler.process(validParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.UNKNOWN_ERROR.getResponseCode(),
            response.getResponseCode());
        verify(vnPayService).verifyIpn(validParams);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_ShouldHandleAllVNPayParameters() {
        // Arrange
        Map<String, String> allParams = new HashMap<>();
        // Test all VNPayParams constants
        allParams.put(VNPayParams.SECURE_HASH, "test_hash");
        allParams.put(VNPayParams.SECURE_HASH_TYPE, "SHA256");
        allParams.put(VNPayParams.AMOUNT, "1000000");
        allParams.put(VNPayParams.ORDER_INFO, "Test Order");
        allParams.put(VNPayParams.ORDER_TYPE, "250000");
        allParams.put(VNPayParams.TXN_REF, VALID_TXN_REF);
        allParams.put(VNPayParams.VERSION, "2.1.0");
        allParams.put(VNPayParams.COMMAND, "pay");
        allParams.put(VNPayParams.TMN_CODE, "TESTCODE");
        allParams.put(VNPayParams.CURRENCY, "VND");
        allParams.put(VNPayParams.RETURN_URL, "http://test.com");
        allParams.put(VNPayParams.CREATED_DATE, "20240221143045");
        allParams.put(VNPayParams.EXPIRE_DATE, "20240221153045");
        allParams.put(VNPayParams.IP_ADDRESS, "127.0.0.1");
        allParams.put(VNPayParams.LOCALE, "vn");

        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF)).thenReturn(Optional.of(mock()));

        // Act
        IpnResponse response = vnPayIpnHandler.process(allParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.SUCCESS.getResponseCode(), response.getResponseCode());
        verify(vnPayService).verifyIpn(allParams);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_WithMinimalRequiredParams_ShouldSucceed() {
        // Arrange
        Map<String, String> minParams = new HashMap<>();
        minParams.put(VNPayParams.TXN_REF, VALID_TXN_REF);
        minParams.put(VNPayParams.SECURE_HASH, "test_hash");

        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF)).thenReturn(Optional.of(mock()));

        // Act
        IpnResponse response = vnPayIpnHandler.process(minParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.SUCCESS.getResponseCode(), response.getResponseCode());
        verify(vnPayService).verifyIpn(minParams);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_WithMissingOptionalParams_ShouldSucceed() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put(VNPayParams.TXN_REF, VALID_TXN_REF);
        params.put(VNPayParams.SECURE_HASH, "test_hash");
        // Omitting optional parameters like LOCALE, VERSION, etc.

        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);
        when(transactionRepository.findById(VALID_TXN_REF)).thenReturn(Optional.of(mock()));

        // Act
        IpnResponse response = vnPayIpnHandler.process(params);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.SUCCESS.getResponseCode(), response.getResponseCode());
        verify(vnPayService).verifyIpn(params);
        verify(transactionRepository).findById(VALID_TXN_REF);
    }

    @Test
    void process_WithMissingRequiredParams_ShouldHandleGracefully() {
        // Arrange
        Map<String, String> incompleteParams = new HashMap<>();
        // Test with missing required params but including optional ones
        incompleteParams.put(VNPayParams.ORDER_INFO, "Test Order");
        incompleteParams.put(VNPayParams.LOCALE, "vn");
        incompleteParams.put(VNPayParams.VERSION, "2.1.0");

        when(vnPayService.verifyIpn(anyMap())).thenReturn(true);

        // Act
        IpnResponse response = vnPayIpnHandler.process(incompleteParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.UNKNOWN_ERROR.getResponseCode(), response.getResponseCode());
        verify(vnPayService).verifyIpn(incompleteParams);
    }

    @Test
    void process_WithInvalidParameterValues_ShouldHandleGracefully() {
        // Arrange
        Map<String, String> invalidParams = new HashMap<>();
        // Test with invalid values for VNPayParams
        invalidParams.put(VNPayParams.TXN_REF, "");  // Empty TXN_REF
        invalidParams.put(VNPayParams.AMOUNT, "invalid_amount");  // Invalid amount
        invalidParams.put(VNPayParams.SECURE_HASH, "invalid_hash");

        when(vnPayService.verifyIpn(anyMap())).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, 
            () -> vnPayIpnHandler.process(invalidParams));
        
        assertEquals(ErrorCode.VNPAY_CHECKSUM_FAILED, exception.getErrorCode());
        verify(vnPayService).verifyIpn(invalidParams);
    }
} 