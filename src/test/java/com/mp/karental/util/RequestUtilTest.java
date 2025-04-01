package com.mp.karental.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void getIpAddress_WhenXForwardedForIsNull_AndRemoteAddrIsNull_ShouldReturnLocalhost() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);

        // Act
        String result = RequestUtil.getIpAddress(request);

        // Assert
        assertEquals("127.0.0.1", result);
    }

    @Test
    void getIpAddress_WhenXForwardedForIsNull_AndRemoteAddrExists_ShouldReturnRemoteAddr() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        String result = RequestUtil.getIpAddress(request);

        // Assert
        assertEquals("192.168.1.1", result);
    }

    @Test
    void getIpAddress_WhenXForwardedForExists_ShouldReturnFirstIp() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18, 150.172.238.178");

        // Act
        String result = RequestUtil.getIpAddress(request);

        // Assert
        assertEquals("203.0.113.195", result);
    }
} 