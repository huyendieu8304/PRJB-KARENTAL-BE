package com.mp.karental.controller;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.request.EditPasswordRequest;
import com.mp.karental.dto.request.EditProfileRequest;
import com.mp.karental.dto.request.CheckUniqueEmailRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Role;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.service.UserService;
import com.mp.karental.validation.validator.UniqueEmailValidator;
import jakarta.validation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;


import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is a class used to test UserController
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UniqueEmailValidator uniqueEmailValidator;


    @Test
    void registerAccount() {
        // Given
        AccountRegisterRequest request = new AccountRegisterRequest();
        request.setFullName("Nguyễn Văn A");
        request.setIsCustomer("true");

        UserResponse expectedUserResponse = new UserResponse();
        expectedUserResponse.setFullName("Nguyễn Văn A");
        expectedUserResponse.setRole(Role.builder().name(ERole.CUSTOMER).build());

        // Configure the service mock to return the expected response
        when(userService.addNewAccount(request)).thenReturn(expectedUserResponse);

        // When
        ApiResponse<UserResponse> result = userController.registerAccount(request);

        // Then
        assertNotNull(result, "ApiResponse should not be null");
        assertEquals(expectedUserResponse, result.getData(), "Returned user response does not match expected");

        // Verify that the service's addNewAccount method was invoked exactly once with the given request
        verify(userService, times(1)).addNewAccount(request);
    }

    /**
     * Test edit profile successfully
     */
    @Test
    void editProfile_Success() {
        // Given
        EditProfileRequest request = new EditProfileRequest();
        request.setFullName("Nguyễn Văn B");

        EditProfileResponse expectedResponse = new EditProfileResponse();
        expectedResponse.setFullName("Nguyễn Văn B");

        when(userService.editProfile(request)).thenReturn(expectedResponse);

        // When
        ApiResponse<EditProfileResponse> result = userController.editProfile(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result.getData());
        verify(userService, times(1)).editProfile(request);
    }


    /**
     * Test edit password successfully
     */
    @Test
    void editPassword_Success() {
        // Given
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newSecurePassword");

        doNothing().when(userService).editPassword(request);

        // When
        ApiResponse<String> result = userController.editPassword(request);

        // Then
        assertNotNull(result);
        verify(userService, times(1)).editPassword(request);
    }
    @Test
    void editProfile_Fail_UserNotFound() {
        EditProfileRequest request = new EditProfileRequest();
        when(userService.editProfile(request)).thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        AppException exception = assertThrows(AppException.class, () -> userController.editProfile(request));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
        verify(userService, times(1)).editProfile(request);
    }


    @Test
    void editPassword_Fail_IncorrectPassword() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newSecurePassword");

        doThrow(new AppException(ErrorCode.INCORRECT_PASSWORD)).when(userService).editPassword(request);

        AppException exception = assertThrows(AppException.class, () -> userController.editPassword(request));
        assertEquals(ErrorCode.INCORRECT_PASSWORD, exception.getErrorCode());
        verify(userService, times(1)).editPassword(request);
    }

    @Test
    void editPassword_Fail_UserNotFound() {
        EditPasswordRequest request = new EditPasswordRequest();
        doThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB)).when(userService).editPassword(request);

        AppException exception = assertThrows(AppException.class, () -> userController.editPassword(request));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
        verify(userService, times(1)).editPassword(request);
    }

    @Test
    void getUserProfile_Success() {
        // Given
        EditProfileResponse expectedResponse = new EditProfileResponse();
        expectedResponse.setFullName("Nguyễn Văn C");

        when(userService.getUserProfile()).thenReturn(expectedResponse);

        // When
        ResponseEntity<ApiResponse<EditProfileResponse>> response = userController.getUserProfile();

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody().getData());
        verify(userService, times(1)).getUserProfile();
    }

    @Test
    void getUserProfile_Fail_UserNotFound() {
        // Given
        when(userService.getUserProfile()).thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // When
        AppException exception = assertThrows(AppException.class, () -> userController.getUserProfile());

        // Then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
        verify(userService, times(1)).getUserProfile();
    }

    @Test
    void shouldPassValidationWhenEmailIsUnique() {
        // Given: Email not existed
        CheckUniqueEmailRequest request = new CheckUniqueEmailRequest("unique@email.com");

        // Mock repo
        when(accountRepository.findByEmail("unique@email.com")).thenReturn(Optional.empty());

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean isValid = uniqueEmailValidator.isValid("unique@email.com", context);
        ApiResponse<String> result = userController.checkUniqueEmail(request);

        assertTrue(isValid);
        assertNotNull(result);
    }

    @Test
    void testResendVerifyEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String expectedMessage = "The verify email is sent successfully. Please check your inbox again and follow instructions to verify your email.";
        when(userService.resendVerifyEmail(email)).thenReturn(expectedMessage);

        // Act
        ApiResponse<String> response = userController.resendVerifyEmail(email);

        // Assert
        assertNotNull(response);
        assertEquals(expectedMessage, response.getMessage());
        verify(userService).resendVerifyEmail(email);
    }

    @Test
    void testVerifyEmail_Success() {
        // Arrange
        String token = "valid-token";
        doNothing().when(userService).verifyEmail(token);

        // Act
        ApiResponse<String> response = userController.verifyEmail(token);

        // Assert
        assertNotNull(response);
        assertEquals("Verify email successfully! Now you can use your account to login.", response.getMessage());
        verify(userService).verifyEmail(token); // Kiểm tra userService.verifyEmail() đã được gọi đúng cách
    }

    @Test
    void testVerifyEmail_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";
        doThrow(new AppException(ErrorCode.INVALID_ONETIME_TOKEN))
                .when(userService).verifyEmail(invalidToken);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            userController.verifyEmail(invalidToken);
        });

        assertEquals(ErrorCode.INVALID_ONETIME_TOKEN, exception.getErrorCode());
        verify(userService).verifyEmail(invalidToken); // Kiểm tra userService.verifyEmail() đã được gọi đúng cách
    }
}