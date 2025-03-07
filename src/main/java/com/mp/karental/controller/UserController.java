package com.mp.karental.controller;

import com.mp.karental.dto.request.*;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.UserMapper;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

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
    private final UserProfileRepository userProfileRepository;
    UserMapper userMapper;

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
                .message("Create account successfully. Please check your email inbox to verify your email address.")
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
                .message("Email is unique.")
                .build();
    }

    @PostMapping("/resend-verify-email")
    ApiResponse<String> resendVerifyEmail(@RequestBody @Valid VerifyEmailRequest request){
        return ApiResponse.<String>builder()
                .message(userService.resendVerifyEmail(request))
                .build();
    }

    /**
     * API to edit user profile
     *
     * @param request the new profile information
     * @return an ApiResponse containing updated user information
     */
    @PutMapping(value = "/edit-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<EditProfileResponse> editProfile(@ModelAttribute @Valid EditProfileRequest request)
    {
        log.info("Editing profile for user: {}", request);
        return ApiResponse.<EditProfileResponse>builder()
                .data(userService.editProfile(request))
                .build();

    }

    /**
     * API to get user profile.
     *
     * @return an ApiResponse containing user profile information
     */
    @GetMapping("/edit-profile")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CAR_OWNER')")
    ResponseEntity<ApiResponse<EditProfileResponse>> getUserProfile() {
        log.info("Fetching user profile");
        return ResponseEntity.ok(
                ApiResponse.<EditProfileResponse>builder()
                        .data(userService.getUserProfile())
                        .build()
        );
    }


    /**
     * Changes the password of the current user.
     *
     * @param request the request containing current, new, and confirm passwords
     * @return a response indicating success or failure
     */
    @PutMapping("/edit-password")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CAR_OWNER')")
    public ApiResponse<String> editPassword(@RequestBody @Valid EditPasswordRequest request) {
        log.info("Changing password for user");
        userService.editPassword(request);
        return ApiResponse.<String>builder()
                .message("Password updated successfully")
                .build();
    }

}
