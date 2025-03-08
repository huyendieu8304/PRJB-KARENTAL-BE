package com.mp.karental.controller;

import com.mp.karental.dto.request.auth.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.auth.LoginResponse;
import com.mp.karental.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    AuthenticationService authenticationService;
    @InjectMocks
    AuthenticationController authenticationController;

    @Test
    void login_shouldCallAuthenticationService() {
        // Given
        LoginRequest loginRequest = new LoginRequest("abc@example.com", "password");
        ApiResponse<LoginResponse> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<?>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authenticationController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void refreshToken_shouldCallAuthenticationService() {
        // Given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ApiResponse<LoginResponse> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<?>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authenticationService.refreshToken(any(HttpServletRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authenticationController.refreshToken(mockRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authenticationService, times(1)).refreshToken(any(HttpServletRequest.class));
    }

    @Test
    void logout_shouldCallAuthenticationService() {
        // Given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ApiResponse<LoginResponse> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<?>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authenticationService.logout(any(HttpServletRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authenticationController.logout(mockRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authenticationService, times(1)).logout(any(HttpServletRequest.class));
    }

}