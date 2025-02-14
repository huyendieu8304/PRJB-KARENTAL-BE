package com.mp.karental.controller;

import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    @PostMapping("/register")
    ApiResponse<UserResponse> registerAccount(@RequestBody @Valid AccountRegisterRequest request){
        log.info("Registering account {}", request);
        return ApiResponse.<UserResponse>builder()
                .data(userService.addNewAccount(request))
                .build();
    }

}
