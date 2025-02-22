package com.mp.karental.service;

import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.AuthenticationResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.RefreshToken;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.jwt.JwtUtils;
import com.mp.karental.security.service.RefreshTokenService;
import com.mp.karental.security.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    AuthenticationManager authenticationManager;
    JwtUtils jwtUtils;
    RefreshTokenService refreshTokenService;

    public ResponseEntity<?> login(LoginRequest request) {
        //authenticate user's login information
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );

        //set Authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        //generate cookies
        //Generate access token cookie
        ResponseCookie accessTokenCookie = jwtUtils.generateAccessTokenCookie(userDetails);

        //Generate refresh token cookie
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getAccoutnId());
        ResponseCookie refreshTokenCookie = jwtUtils.generateRefreshTokenCookie(refreshToken.getToken());

        //put role in response
        String role = userDetails.getRole().getName().toString();
        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .data(new AuthenticationResponse(role))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString()) //return access token to cookies
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()) //return refresh token to cookies
                .body(apiResponse);

    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        //get the refresh token out from cookies
        String refreshToken = jwtUtils.getRefreshTokenFromCookie(request);

        //refresh token not exist in the cookies
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //find the token in the db
        RefreshToken refreshTk = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN)); //refresh token not exist in db

        //verify the expiration of the refresh token
        refreshTokenService.verifyExpiration(refreshTk);

        //refresh token exist in db and not expired
        Account account = refreshTk.getAccount();
        //create new access token cookie
        ResponseCookie accessTokenCookie = jwtUtils.generateAccessTokenCookie(account);

//        //delete old refresh token in db
//        refreshTokenService.deleteToken(refreshTk);
        //create new refresh token cookie
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(account.getId()); //save to db
        ResponseCookie refreshTokenCookie = jwtUtils.generateRefreshTokenCookie(newRefreshToken.getToken());

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data("Successfully refresh token")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(apiResponse);
    }


    public ResponseEntity<?> logout(HttpServletRequest request) {

        //get the refresh token out from cookies
        String refreshToken = jwtUtils.getRefreshTokenFromCookie(request);

        //TODO: invalidate những cái access token mà nó còn hạn

        //refresh token exist in cookie
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }


        ResponseCookie accessTokenCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshTokenCookie = jwtUtils.getCleanJwtRefreshCookie();
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data("Successfully logged out")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(apiResponse);
    }
}
