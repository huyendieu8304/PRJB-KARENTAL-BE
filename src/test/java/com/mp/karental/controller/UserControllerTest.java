package com.mp.karental.controller;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Role;
import com.mp.karental.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}