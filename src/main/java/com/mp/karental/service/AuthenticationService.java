package com.mp.karental.service;

import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.LoginResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.security.entity.InvalidateAccessToken;
import com.mp.karental.security.entity.InvalidateRefreshToken;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.security.repository.InvalidateAccessTokenRepo;
import com.mp.karental.security.repository.InvalidateRefreshTokenRepo;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.entity.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;


@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {


    @Value("${application.security.jwt.access-token-cookie-name}")
    @NonFinal
    private String accessTokenCookieName;

    @Value("${server.servlet.context-path}")
    @NonFinal
    private String contextPath;

    @Value("${application.security.jwt.access-token-expiration}")
    @NonFinal
    private long accessTokenExpiration;
    //=======================================
    @NonFinal
    private String refreshTokenUrl = "/karental/auth/refresh-token";

    @Value("${application.security.jwt.refresh-token-cookie-name}")
    @NonFinal
    private String refreshTokenCookieName;

    @Value("${application.security.jwt.refresh-token-expiration}")
    @NonFinal
    private long refreshTokenExpiration;
    //=======================================
    //TODO: check this again
    @NonFinal
    private String logoutUrl = "/karental/auth/logout";


    AuthenticationManager authenticationManager;
    JwtUtils jwtUtils;
    InvalidateAccessTokenRepo invalidateAccessTokenRepo;
    InvalidateRefreshTokenRepo invalidateRefreshTokenRepo;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> login(LoginRequest request) {
        //authenticate user's login information
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );

        //set Authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        //generate tokens
        String accessToken = jwtUtils.generateAccessTokenFromUserEmail(userDetails.getEmail());
        String refreshToken = jwtUtils.generateRefreshTokenFromAccountId(userDetails.getAccoutnId());

        //put role in response
        String role = userDetails.getRole().getName().toString();
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .data(new LoginResponse(role))
                .build();

        return sendApiResponseResponseEntity(accessToken, refreshToken, apiResponse);

    }

    private ResponseEntity<ApiResponse<?>> sendApiResponseResponseEntity(String accessToken, String refreshToken, ApiResponse<?> apiResponse) {
        //Generate token cookie
        ResponseCookie accessTokenCookie = generateCookie(accessTokenCookieName, accessToken, contextPath, accessTokenExpiration);
        ResponseCookie refreshTokenCookie = generateCookie(refreshTokenCookieName, refreshToken, refreshTokenUrl, refreshTokenExpiration);
        ResponseCookie accessTokenCookieLogout = generateCookie(accessTokenCookieName, accessToken, logoutUrl, accessTokenExpiration);
        ResponseCookie refreshTokenCookieLogout = generateCookie(refreshTokenCookieName, accessToken, logoutUrl, refreshTokenExpiration);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieLogout.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieLogout.toString())
                .body(apiResponse);
    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        //get the refresh token out from cookies
        String refreshToken = getCookieValueByName(request, refreshTokenCookieName);

        //refresh token not exist in the cookies
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //validate jwt refresh token
        if(jwtUtils.validateJwtRefreshToken(refreshToken)){
            //the refresh token still not expire but found in invalidated table
            if (invalidateRefreshTokenRepo.findByToken(refreshToken).isPresent()) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            //save to invalidate table
            invalidateRefreshTokenRepo.save(InvalidateRefreshToken.builder()
                    .token(refreshToken)
                    .expiresAt(jwtUtils.getExpirationDateFromRefreshToken(refreshToken))
                    .build());
        }

        //get user account's id from refresh token to generate new access token
        String accountId = jwtUtils.getUserAccountIdFromRefreshToken(refreshToken);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        //the account in the token is inactive (banned)
        if (!account.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
        }

        //generate tokens
        String newAccessToken = jwtUtils.generateAccessTokenFromUserEmail(account.getEmail());
        String newRefreshToken = jwtUtils.generateRefreshTokenFromAccountId(account.getId());

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data("Successfully refresh token")
                .build();

        return sendApiResponseResponseEntity(newAccessToken, newRefreshToken, apiResponse);
    }

    public ResponseEntity<?> logout(HttpServletRequest request) {

        //get tokens out from cookies
        String accessToken = getCookieValueByName(request, accessTokenCookieName);
        String refreshToken = getCookieValueByName(request, refreshTokenCookieName);


        //refresh token exist in cookie
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                jwtUtils.validateJwtRefreshToken(refreshToken);
                //the refresh token still not expire

                invalidateRefreshTokenRepo.save(InvalidateRefreshToken.builder()
                        .token(refreshToken)
                        .expiresAt(jwtUtils.getExpirationDateFromRefreshToken(refreshToken))
                        .build());
            } catch (Exception e) {
                //TODO: suar laij phaan nay
                System.out.println("Invalid refresh token, maybe it's expired");
            }
        }

        //access token exist in cookie
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                jwtUtils.validateJwtAccessToken(accessToken);
                //the access token still not expire
                invalidateAccessTokenRepo.save(InvalidateAccessToken.builder()
                        .token(refreshToken)
                        .expiresAt(jwtUtils.getExpirationDateFromAccessToken(accessToken))
                        .build());
            } catch (Exception e) {
                //TODO: suar laij phaan nay
                System.out.println("Invalid access token, maybe it's expired");
            }
        }

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data("Successfully logged out")
                .build();
        return sendApiResponseResponseEntity(null, null, apiResponse);
    }

    /**
     * Create cookie to return to the client
     *
     * @param cookieName  the name of the cookie
     * @param cookieValue the value of the cookie
     * @param path        the domain that this cookie sent along
     * @return ResponseCookie object
     */
    private ResponseCookie generateCookie(String cookieName, String cookieValue, String path, long maxAgeMiliseconds) {
        return ResponseCookie
                .from(cookieName, cookieValue)
                .path(path)
                .maxAge(maxAgeMiliseconds / 1000) // seconds ~ 1days
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    /**
     * get the value of the cookie using its' name (key-value)
     *
     * @param request
     * @param name
     * @return
     */
    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

}

