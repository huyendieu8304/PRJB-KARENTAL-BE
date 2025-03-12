package com.mp.karental.controller;

import com.mp.karental.dto.request.user.AccountRegisterRequest;
import com.mp.karental.dto.request.user.CheckUniqueEmailRequest;
import com.mp.karental.dto.request.user.EditPasswordRequest;
import com.mp.karental.dto.request.user.EditProfileRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.user.EditProfileResponse;
import com.mp.karental.dto.response.user.UserResponse;
import com.mp.karental.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
@Validated
@Slf4j
@Tag(name = "User", description = "Operations about user")
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
     */
    @PostMapping("/register")
    @Operation(
            summary = "Create a new account",
            description = "User create a new Customer or Car Owner account",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        description = "Success",
                            responseCode = "200"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        description = "Bad request",
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    ApiResponse<UserResponse> registerAccount(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Information of new user",
                    content = @Content(schema = @Schema(implementation = AccountRegisterRequest.class))
            )
            @Valid AccountRegisterRequest request){
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

    @GetMapping("/resend-verify-email/{email}")
    ApiResponse<String> resendVerifyEmail(@PathVariable("email")
                                          @Email(message = "INVALID_EMAIL")
                                          String email){
        return ApiResponse.<String>builder()
                .message(userService.resendVerifyEmail(email))
                .build();
    }


    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@RequestParam("t") String verifyEmailToken) {
        userService.verifyEmail(verifyEmailToken);
        return ApiResponse.<String>builder()
                .message("Verify email successfully! Now you can use your account to login.")
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
