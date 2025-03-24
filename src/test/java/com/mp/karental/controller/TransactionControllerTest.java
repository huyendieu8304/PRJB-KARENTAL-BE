package com.mp.karental.controller;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.dto.request.TransactionRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.ListTransactionResponse;
import com.mp.karental.dto.response.TransactionPaymentURLResponse;
import com.mp.karental.dto.response.TransactionResponse;
import com.mp.karental.service.TransactionService;
import com.mp.karental.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    @Mock
    private TransactionService transactionService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(1000);
    }

    @Test
    void testTopUp() {
        when(RequestUtil.getIpAddress(httpServletRequest)).thenReturn("127.0.0.1");
        when(transactionService.createTransactionTopUp(any())).thenReturn(new TransactionPaymentURLResponse());

        ApiResponse<TransactionPaymentURLResponse> response = transactionController.topUp(transactionRequest, httpServletRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        verify(transactionService, times(1)).createTransactionTopUp(any());
    }

    @Test
    void testWithdraw() {
        when(RequestUtil.getIpAddress(httpServletRequest)).thenReturn("127.0.0.1");
        when(transactionService.withdraw(anyLong())).thenReturn(new TransactionResponse());

        ApiResponse<TransactionResponse> response = transactionController.withdraw(transactionRequest, httpServletRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        verify(transactionService, times(1)).withdraw(anyLong());
    }

    @Test
    void testGetAllTransactionResponseList() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(transactionService.getAllTransactions(from, to)).thenReturn(new ListTransactionResponse());

        ApiResponse<ListTransactionResponse> response = transactionController.getAllTransactionResponseList(from, to);

        assertNotNull(response);
        assertNotNull(response.getData());
        verify(transactionService, times(1)).getAllTransactions(from, to);
    }

    @Test
    void testGetTransactionFailed() {
        String transactionId = "123";
        when(transactionService.getTransactionStatus(eq(transactionId), anyMap())).thenReturn(new TransactionResponse());

        ApiResponse<TransactionResponse> response = transactionController.getTransaction(transactionId, Map.of());

        assertNotNull(response);
        assertNotNull(response.getData());
        verify(transactionService, times(1)).getTransactionStatus(eq(transactionId), anyMap());
    }
    @Test
    void testGetTransaction() {
        String transactionId = "191f08fc-cb22-460d-8771-9b714fc18ad3";
        when(transactionService.getTransactionStatus(eq(transactionId), anyMap())).thenReturn(new TransactionResponse());

        ApiResponse<TransactionResponse> response = transactionController.getTransaction(transactionId, Map.of());

        assertNotNull(response);
        assertNotNull(response.getData());
        verify(transactionService, times(1)).getTransactionStatus(eq(transactionId), anyMap());
    }

    @Test
    void testGetAllTransactionList_WhenAllIsTrue_ShouldReturnAllTransactions() {
        // Arrange
        when(transactionService.getAllTransactionsList()).thenReturn(new ListTransactionResponse());

        // Act
        ApiResponse<ListTransactionResponse> response = transactionController.getAllTransactionList(true);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        verify(transactionService, times(1)).getAllTransactionsList();
    }

    @Test
    void testGetAllTransactionList_WhenAllIsFalse_ShouldReturnEmptyList() {
        // Act
        ApiResponse<ListTransactionResponse> response = transactionController.getAllTransactionList(false);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        verifyNoInteractions(transactionService);
    }
}
