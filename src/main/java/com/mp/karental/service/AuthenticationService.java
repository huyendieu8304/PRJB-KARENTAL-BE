package com.mp.karental.service;

import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.AuthenticationResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Token;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.security.JwtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;


    public AuthenticationResponse login(LoginRequest request) {

        //Get account from the repository
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_LOGIN_INFORMATION));

        //Verify the password
        boolean isRightPassword = passwordEncoder.matches(request.getPassword(), account.getPassword());

        if (!isRightPassword) { //wrong password
            throw new AppException(ErrorCode.INVALID_LOGIN_INFORMATION);
        }

        //TODO: Xem lại chỗ này coi có cần cái Token Object này không
        //User is authenticate, generate Token
        Token token = Token.builder()
                .account(account)
                .accessToken(jwtService.generateAccessToken(account))
                .refreshToken(jwtService.generateRefreshToken(account))
                .build();
//        //save the token to the database
//        tokenRepository.save(token);

        return AuthenticationResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .userRole(account.getRole().getName().toString())
                .build();
    }




}
