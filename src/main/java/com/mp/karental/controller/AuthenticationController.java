package com.mp.karental.controller;

import com.mp.karental.dto.request.CheckUniqueEmailRequest;
import com.mp.karental.dto.request.ForgotPasswordRequest;
import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.request.VerifyEmailRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@RequestParam("t") String verifyEmailToken) {
        authenticationService.verifyEmail(verifyEmailToken);
        return ApiResponse.<String>builder()
                .message("Verify email successfully! Now you can use your account to login.")
                .build();
    }

    //the user forgot password, send request to change the password
//    @GetMapping("/change-password")
//    public ApiResponse<String> requestChangePassword(ForgotPasswordRequest request) {
//        A
//    }
}
