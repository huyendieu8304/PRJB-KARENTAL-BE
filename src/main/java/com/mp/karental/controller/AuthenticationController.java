package com.mp.karental.controller;

import com.mp.karental.dto.request.auth.ChangePasswordRequest;
import com.mp.karental.dto.request.auth.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Handle request relating to authentication and authorization
 * eg: login, logout, refresh token
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(HttpServletRequest request) {
        return authenticationService.refreshToken(request);
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        return authenticationService.logout(request);
    }


    //the user forgot password, send request to change the password
    @GetMapping("/forgot-password/{email}")
    public ApiResponse<String> sendForgotPasswordEmail(@PathVariable("email")
                                                         @Email(message = "INVALID_EMAIL")
                                                         String email) {
        authenticationService.sendForgotPasswordEmail(email);
        return ApiResponse.<String>builder()
                .message("An email has been send to your email address. Please click to the link in the email to change password.")
                .build();

    }

    @GetMapping("/forgot-password/verify")
    public ApiResponse<String> verifyForgotPassword(@RequestParam("t") String forgotPasswordToken) {
        return ApiResponse.<String>builder()
                .message("Verify change password request successfully!")
                .data(authenticationService.verifyForgotPassword(forgotPasswordToken))
                .build();
    }

    @PostMapping("/forgot-password/change")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request){
        authenticationService.changePassword(request);
        return ApiResponse.<String>builder()
                .message("Change password successfully!")
                .build();
    }
}
