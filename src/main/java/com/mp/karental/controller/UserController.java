package com.mp.karental.controller;

import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.request.CheckUniqueEmailRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling user-related operations.
 * <p>
 * This controller provides endpoints for user management functionalities,
 * including user registration.
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;
    /**
     * Registers a new user account.
     * <p>
     * This method accepts a validated {@code AccountRegisterRequest} containing
     * the registration details, delegates the account creation to the {@code UserService},
     * and wraps the resulting {@code UserResponse} in a standardized {@code ApiResponse}.
     * </p>
     *
     * @param request the registration details for the new account
     * @return an {@code ApiResponse} containing the created user information
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @PostMapping("/register")
    ApiResponse<UserResponse> registerAccount(@RequestBody @Valid AccountRegisterRequest request){
        log.info("Registering account {}", request);
        return ApiResponse.<UserResponse>builder()
                .data(userService.addNewAccount(request))
                .build();
    }

    /**
     * this method check whether the email exist in the db or not
     * @param request Object contain email
     * @return ApiResponse Object
     */
    @PostMapping("/check-unique-email")
    ApiResponse<String> checkUniqueEmail(@RequestBody @Valid CheckUniqueEmailRequest request){
        return ApiResponse.<String>builder()
                .build();
    }



}
