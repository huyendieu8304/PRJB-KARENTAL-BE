package com.mp.karental.controller;

import com.mp.karental.dto.request.auth.ChangePasswordRequest;
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
        ResponseEntity<ApiResponse<LoginResponse>> mockResponse = ResponseEntity.ok(apiResponse);

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
        ApiResponse<String> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<String>> mockResponse = ResponseEntity.ok(apiResponse);

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
        ApiResponse<String> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<String>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authenticationService.logout(any(HttpServletRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authenticationController.logout(mockRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authenticationService, times(1)).logout(any(HttpServletRequest.class));
    }

    @Test
    void testSendForgotPasswordEmail_ValidEmail() {
        // Arrange
        String email = "test@example.com";

        // Act
        ApiResponse<String> response = authenticationController.sendForgotPasswordEmail(email);

        // Assert
        assertEquals("An email has been send to your email address. Please click to the link in the email to change password.",
                response.getMessage());
        verify(authenticationService, times(1)).sendForgotPasswordEmail(email);
    }

    @Test
    void testVerifyForgotPassword_ValidToken() {
        // Arrange
        String token = "valid-token";
        String expectedResponse = "AccountId123";
        when(authenticationService.verifyForgotPassword(token)).thenReturn(expectedResponse);

        // Act
        ApiResponse<String> response = authenticationController.verifyForgotPassword(token);

        // Assert
        assertEquals("Verify change password request successfully!", response.getMessage());
        assertEquals(expectedResponse, response.getData());
        verify(authenticationService, times(1)).verifyForgotPassword(token);
    }

    @Test
    void testVerifyForgotPassword_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(authenticationService.verifyForgotPassword(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authenticationController.verifyForgotPassword(token)
        );
        assertEquals("Invalid token", exception.getMessage());
        verify(authenticationService, times(1)).verifyForgotPassword(token);
    }

    @Test
    void testChangePassword_ValidRequest() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setForgotPasswordToken("valid-token");
        request.setNewPassword("NewP@ssword123");

        doNothing().when(authenticationService).changePassword(request);

        // Act
        ApiResponse<String> response = authenticationController.changePassword(request);

        // Assert
        assertEquals("Change password successfully!", response.getMessage());
        verify(authenticationService, times(1)).changePassword(request);
    }

}