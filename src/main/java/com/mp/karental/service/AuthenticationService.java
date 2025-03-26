package com.mp.karental.service;

import com.mp.karental.dto.request.auth.ChangePasswordRequest;
import com.mp.karental.dto.request.auth.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.auth.LoginResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.entity.UserDetailsImpl;
import com.mp.karental.security.service.TokenService;
import com.mp.karental.util.RedisUtil;
import jakarta.mail.MessagingException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

/**
 * Service class for handling authentication operations.
 *
 * @author DieuTTH4
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    @Value("${front-end.base-url}")
    @NonFinal
    private String frontEndBaseUrl;

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
    @Value("${application.domain}")
    @NonFinal
    private String domain;


    AuthenticationManager authenticationManager;

    JwtUtils jwtUtils;
    RedisUtil redisUtil;
    PasswordEncoder passwordEncoder;

    TokenService tokenService;
    EmailService emailService;

    AccountRepository accountRepository;
    UserProfileRepository userProfileRepository;

    public ResponseEntity<ApiResponse<LoginResponse>> login(LoginRequest request) {
        log.info("Processing login request, email={}", request.getEmail());
        //authenticate user's login information
        Authentication authentication = null;

        try {
            authentication = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                    );
        } catch (InternalAuthenticationServiceException e) {
            throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
//        } catch (UsernameNotFoundException e) {
//            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB);
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_LOGIN_INFORMATION);
        }

        //set Authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        //generate tokens
        String accessToken = jwtUtils.generateAccessTokenFromUserEmail(userDetails.getEmail());
        String refreshToken = jwtUtils.generateRefreshTokenFromAccountId(userDetails.getAccoutnId());

        //put user's role and fullname in response
        String role = userDetails.getRole().getName().toString();
        String fullName = userProfileRepository.findById(userDetails.getAccoutnId()).get().getFullName();
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .data(new LoginResponse(role, fullName))
                .build();
        log.info("Account with email={} logged in successfully", request.getEmail());
        return  sendApiResponseResponseEntity(accessToken, refreshToken, apiResponse);
    }

    private <T> ResponseEntity<ApiResponse<T>> sendApiResponseResponseEntity(String accessToken, String refreshToken, ApiResponse<T> apiResponse) {
        //Generate token cookie
        ResponseCookie accessTokenCookie = generateCookie(accessTokenCookieName, accessToken, contextPath, accessTokenExpiration);
        ResponseCookie refreshTokenCookie = generateCookie(refreshTokenCookieName, refreshToken, refreshTokenUrl, refreshTokenExpiration);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(apiResponse);
    }

    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request) {
        log.info("Processing refresh token request");
        //get the refresh token out from cookies
        String refreshToken = getCookieValueByName(request, refreshTokenCookieName);

        //refresh token not exist in the cookies
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //validate jwt refresh token
        if (jwtUtils.validateJwtRefreshToken(refreshToken)) {
            //the refresh token still not expire but found invalidated
            if (tokenService.isRefreshTokenInvalidated(refreshToken)) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            //save old refresh token to invalidate table
            tokenService.invalidateRefreshToken(refreshToken, jwtUtils.getExpirationAtFromRefreshToken(refreshToken));
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
        log.info("New refresh token is generated for account with email={}", account.getEmail());
        return sendApiResponseResponseEntity(newAccessToken, newRefreshToken, apiResponse);
    }

    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        log.info("Processing refresh token request");
        //get tokens out from cookies
        String accessToken = getCookieValueByName(request, accessTokenCookieName);
        String refreshToken = getCookieValueByName(request, refreshTokenCookieName);


        //refresh token exist in cookie
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                jwtUtils.validateJwtRefreshToken(refreshToken);
                //the refresh token still not expire, invalidate it by saving to redis
                tokenService.invalidateRefreshToken(refreshToken, jwtUtils.getExpirationAtFromRefreshToken(refreshToken));
            } catch (Exception e) {
                log.info("Invalid refresh token, user can not refresh access token with this refresh token -> successfully logout ");
            }
        }

        //access token exist in cookie
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                jwtUtils.validateJwtAccessToken(accessToken);
                //the access token still not expire
                tokenService.invalidateAccessToken(accessToken, jwtUtils.getExpirationAtFromAccessToken(accessToken));

            } catch (Exception e) {
                log.info("Invalid access token, user can not be authenticated with this access token -> successfully logout ");
            }
        }

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data("Successfully logged out")
                .build();
        log.info("Logged out successfully");
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
                .domain(domain)
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

    //=====================================================
    //FORGOT PASSWORD

    public void sendForgotPasswordEmail(String email) {
        log.info("user with email={} request to change password.", email);
        //is the email used by one account in the system
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT));

        //The email in the account is not verified or the account is inactivated
        if (!account.isEmailVerified() || !account.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
        }

        //send email
        String changePasswordToken = redisUtil.generateForgotPasswordToken(account.getId());
        String forgotPasswordUrl = frontEndBaseUrl + "/auth/forgot-password/verify?t=" + changePasswordToken;
        log.info("Verify email url: {}", forgotPasswordUrl);
        try {
            emailService.sendForgotPasswordEmail(email, forgotPasswordUrl);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_FORGOT_PASSWORD_EMAIL_TO_USER_FAIL);
        }
    }

    /**
     * verify that the user really forgot the password
     *
     * @param forgotPasswordToken
     * @return  change password token if the token is valid
     */
    public String verifyForgotPassword(String forgotPasswordToken) {
        log.info("user with token={} request to change password.", forgotPasswordToken);
        //verify forgot password token
        verifyForgotPasswordToken(forgotPasswordToken);

        //generate  change password token
        return forgotPasswordToken;
    }

    private Account verifyForgotPasswordToken(String forgotPasswordToken) {
        String accountId = redisUtil.getValueOfForgotPasswordToken(forgotPasswordToken);
        //token exist and not expired
        if (accountId != null && !accountId.isEmpty()) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
            //The email in the account is not verified or the account is inactivated
            if (!account.isEmailVerified() || !account.isActive()) {
                throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
            }
            return account;
        } else {
            log.info("Forgot password token is invalid!");
            throw new AppException(ErrorCode.INVALID_FORGOT_PASSWORD_TOKEN);
        }
    }

    public void changePassword(ChangePasswordRequest request) {
        log.info("change password ");
        //Verify the forgot password token
        Account account = verifyForgotPasswordToken(request.getForgotPasswordToken());
        //update password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        log.info("Changed password successfully");
        //delete the forgot password token
        redisUtil.deleteForgotPasswordToken(request.getForgotPasswordToken());
    }

}

