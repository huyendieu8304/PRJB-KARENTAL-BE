package com.mp.karental.payment.service;

import com.mp.karental.payment.configuration.PaymentConfig;
import com.mp.karental.payment.constant.VNPayParams;
import com.mp.karental.payment.dto.request.InitPaymentRequest;
import com.mp.karental.payment.dto.response.InitPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @Mock
    private PaymentConfig paymentConfig;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private VNPayService vnPayService;

    private static final String TEST_TMN_CODE = "TEST_TMN_CODE";
    private static final String TEST_INIT_PAYMENT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String TEST_RETURN_URL = "http://localhost:8080/payment/vnpay_return/%s";
    private static final Integer TEST_TIMEOUT = 15;
    private static final String TEST_SECURE_HASH = "test_secure_hash";

    @BeforeEach
    void setUp() {
        // Configure payment config mock
        when(paymentConfig.getTmnCode()).thenReturn(TEST_TMN_CODE);
        when(paymentConfig.getInitPaymentUrl()).thenReturn(TEST_INIT_PAYMENT_URL);
        when(paymentConfig.getReturnUrl()).thenReturn(TEST_RETURN_URL);
        when(paymentConfig.getTimeout()).thenReturn(TEST_TIMEOUT);

        // Initialize VNPayService
        vnPayService.init();
    }

    @Test
    void initPayment_WithValidRequest_ShouldReturnValidResponse() {
        // Arrange
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef("TXN123")
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getVnpUrl());
        assertTrue(response.getVnpUrl().startsWith(TEST_INIT_PAYMENT_URL));
        assertTrue(response.getVnpUrl().contains("vnp_SecureHash=" + TEST_SECURE_HASH));
        
        // Verify required parameters in URL
        String url = response.getVnpUrl();
        assertTrue(url.contains("vnp_Version=" + VNPayService.VERSION));
        assertTrue(url.contains("vnp_Command=pay"));
        assertTrue(url.contains("vnp_TmnCode=" + TEST_TMN_CODE));
        assertTrue(url.contains("vnp_Amount=100000000")); // Amount * 100
        assertTrue(url.contains("vnp_TxnRef=TXN123"));
        
        verify(cryptoService).sign(any());
    }

    @Test
    void verifyIpn_WithValidParams_ShouldReturnTrue() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "100000000");
        params.put("vnp_TxnRef", "TXN123");
        params.put(VNPayParams.SECURE_HASH, TEST_SECURE_HASH);
        
        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        boolean result = vnPayService.verifyIpn(params);

        // Assert
        assertTrue(result);
        verify(cryptoService).sign(any());
    }

    @Test
    void verifyIpn_WithInvalidSecureHash_ShouldReturnFalse() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "100000000");
        params.put("vnp_TxnRef", "TXN123");
        params.put(VNPayParams.SECURE_HASH, "invalid_hash");
        
        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        boolean result = vnPayService.verifyIpn(params);

        // Assert
        assertFalse(result);
        verify(cryptoService).sign(any());
    }

    @Test
    void initPayment_ShouldIncludeCorrectAmount() {
        // Arrange
        long originalAmount = 1000000L;
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(originalAmount)
                .txnRef("TXN123")
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        assertTrue(url.contains("vnp_Amount=" + (originalAmount * VNPayService.DEFAULT_MULTIPLIER)));
    }

    @Test
    void initPayment_ShouldIncludeCorrectOrderInfo() {
        // Arrange
        String txnRef = "TXN123";
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef(txnRef)
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        String expectedOrderInfo = URLDecoder.decode(
            url.substring(url.indexOf("vnp_OrderInfo=") + 14, url.indexOf("&", url.indexOf("vnp_OrderInfo="))),
            StandardCharsets.UTF_8
        );
        assertEquals("Top-Up transaction " + txnRef, expectedOrderInfo);
    }

    private String extractUrlParameter(String url, String paramName) {
        String searchString = paramName + "=";
        int startIndex = url.indexOf(searchString);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchString.length();
        int endIndex = url.indexOf("&", startIndex);
        if (endIndex == -1) {
            endIndex = url.length();
        }
        String encodedValue = url.substring(startIndex, endIndex);
        return URLDecoder.decode(encodedValue, StandardCharsets.US_ASCII);
    }

    @Test
    void initPayment_ShouldIncludeCorrectReturnUrl() {
        // Arrange
        String txnRef = "TXN123";
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef(txnRef)
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        String expectedReturnUrl = String.format(TEST_RETURN_URL, txnRef);
        String actualReturnUrl = extractUrlParameter(url, "vnp_ReturnUrl");
        
        assertEquals(expectedReturnUrl, actualReturnUrl,
            "Return URL in payment URL should match expected format");
    }

    @Test
    void verifyIpn_WithNullValues_ShouldSkipNullFields() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "100000000");
        params.put("vnp_TxnRef", null);
        params.put(VNPayParams.SECURE_HASH, TEST_SECURE_HASH);
        
        // Use ArgumentMatcher to match the string containing "vnp_Amount=100000000"
        when(cryptoService.sign(argThat(str -> 
            str.contains("vnp_Amount=100000000"))))
            .thenReturn(TEST_SECURE_HASH);

        // Act
        boolean result = vnPayService.verifyIpn(params);

        // Assert
        assertTrue(result);
        verify(cryptoService).sign(argThat(str -> 
            str.contains("vnp_Amount=100000000")));
    }

    @Test
    void verifyIpn_WithEmptyParams_ShouldHandleGracefully() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put(VNPayParams.SECURE_HASH, TEST_SECURE_HASH);
        
        // Use lenient stubbing for empty string
        lenient().when(cryptoService.sign("")).thenReturn(TEST_SECURE_HASH);

        // Act
        boolean result = vnPayService.verifyIpn(params);

        // Assert
        assertTrue(result);
        verify(cryptoService).sign("");
    }

    @Test
    void initPayment_ShouldIncludeCorrectTimeoutValue() {
        // Arrange
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef("TXN123")
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        String createDate = extractUrlParameter(url, "vnp_CreateDate");
        String expireDate = extractUrlParameter(url, "vnp_ExpireDate");
        
        assertNotNull(createDate, "Create date should not be null");
        assertNotNull(expireDate, "Expire date should not be null");
        assertEquals(14, createDate.length(), "Create date should be in yyyyMMddHHmmss format");
        assertEquals(14, expireDate.length(), "Expire date should be in yyyyMMddHHmmss format");
    }

    @Test
    void initPayment_ShouldIncludeAllRequiredParameters() {
        // Arrange
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef("TXN123")
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        
        // Verify all required parameters are present
        assertNotNull(extractUrlParameter(url, "vnp_Version"));
        assertNotNull(extractUrlParameter(url, "vnp_Command"));
        assertNotNull(extractUrlParameter(url, "vnp_TmnCode"));
        assertNotNull(extractUrlParameter(url, "vnp_Amount"));
        assertNotNull(extractUrlParameter(url, "vnp_CurrCode"));
        assertNotNull(extractUrlParameter(url, "vnp_TxnRef"));
        assertNotNull(extractUrlParameter(url, "vnp_OrderInfo"));
        assertNotNull(extractUrlParameter(url, "vnp_OrderType"));
        assertNotNull(extractUrlParameter(url, "vnp_Locale"));
        assertNotNull(extractUrlParameter(url, "vnp_ReturnUrl"));
        assertNotNull(extractUrlParameter(url, "vnp_IpAddr"));
        assertNotNull(extractUrlParameter(url, "vnp_CreateDate"));
        assertNotNull(extractUrlParameter(url, "vnp_ExpireDate"));
        assertNotNull(extractUrlParameter(url, "vnp_SecureHash"));
    }

    @Test
    void initPayment_ShouldUseCorrectLocaleAndCurrency() {
        // Arrange
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef("TXN123")
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        assertEquals("en", extractUrlParameter(url, "vnp_Locale"));
        assertEquals("VND", extractUrlParameter(url, "vnp_CurrCode"));
    }

    @Test
    void initPayment_ShouldUseCorrectOrderType() {
        // Arrange
        InitPaymentRequest request = InitPaymentRequest.builder()
                .amount(1000000L)
                .txnRef("TXN123")
                .requestId("REQ123")
                .ipAddress("127.0.0.1")
                .build();

        when(cryptoService.sign(any())).thenReturn(TEST_SECURE_HASH);

        // Act
        InitPaymentResponse response = vnPayService.initPayment(request);

        // Assert
        String url = response.getVnpUrl();
        assertEquals(VNPayService.ORDER_TYPE, extractUrlParameter(url, "vnp_OrderType"));
    }
} 