package com.mp.karental.controller;

import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.AuthenticationResponse;
import com.mp.karental.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authenticationService.login(loginRequest))
                .build();
    }





}
