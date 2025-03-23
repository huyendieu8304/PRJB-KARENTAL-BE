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
        when(vnPayService.verifyIpn(validParams)).thenReturn(true);
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
        when(vnPayService.verifyIpn(validParams)).thenReturn(false);

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
        when(vnPayService.verifyIpn(validParams)).thenReturn(true);
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
        when(vnPayService.verifyIpn(validParams)).thenReturn(true);
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
        when(vnPayService.verifyIpn(emptyParams)).thenReturn(false);

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
        validParams.remove(VNPayParams.TXN_REF);
        when(vnPayService.verifyIpn(validParams)).thenReturn(true);

        // Act
        IpnResponse response = vnPayIpnHandler.process(validParams);

        // Assert
        assertNotNull(response);
        assertEquals(VNPayIPNResponseConst.UNKNOWN_ERROR.getResponseCode(), 
            response.getResponseCode());
        verify(vnPayService).verifyIpn(validParams);
    }

    @Test
    void process_WithMultipleConsecutiveCalls_ShouldHandleEachIndependently() {
        // Arrange
        when(vnPayService.verifyIpn(validParams)).thenReturn(true);
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
} 