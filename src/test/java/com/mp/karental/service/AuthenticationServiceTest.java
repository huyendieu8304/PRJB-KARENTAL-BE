package com.mp.karental.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.LoginResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.entity.InvalidateRefreshToken;
import com.mp.karental.security.entity.UserDetailsImpl;
import com.mp.karental.security.repository.InvalidateAccessTokenRepo;
import com.mp.karental.security.repository.InvalidateRefreshTokenRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test for authentication service
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private InvalidateAccessTokenRepo invalidateAccessTokenRepo;

    @Mock
    private InvalidateRefreshTokenRepo invalidateRefreshTokenRepo;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationService authenticationService;


    @BeforeEach
    void setup() {
        // Set up share variable in generate cookie
        ReflectionTestUtils.setField(authenticationService, "accessTokenCookieName", "accessToken");
        ReflectionTestUtils.setField(authenticationService, "refreshTokenCookieName", "refreshToken");
        ReflectionTestUtils.setField(authenticationService, "contextPath", "/myApp");
        ReflectionTestUtils.setField(authenticationService, "accessTokenExpiration", 3600L);
        ReflectionTestUtils.setField(authenticationService, "refreshTokenExpiration", 7200L);
        ReflectionTestUtils.setField(authenticationService, "refreshTokenUrl", "/karental/auth/refresh-token");
        ReflectionTestUtils.setField(authenticationService, "logoutUrl", "/karental/auth/logout");
    }


    @Test
    void login_successful() {
        // Given
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");

        // mock UserDetailsImpl
        UserDetailsImpl dummyUser = mock(UserDetailsImpl.class);
        when(dummyUser.getEmail()).thenReturn("user@example.com");
        when(dummyUser.getAccoutnId()).thenReturn(String.valueOf(1L));

        // mock role of user
        Role dummyRole = mock(Role.class);
        when(dummyRole.getName()).thenReturn(ERole.CUSTOMER);
        when(dummyUser.getRole()).thenReturn(dummyRole);

        // Mock Authentication with principal is dummyUser
        Authentication authentication = new UsernamePasswordAuthenticationToken(dummyUser, null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Mock generate tokens
        when(jwtUtils.generateAccessTokenFromUserEmail("user@example.com"))
                .thenReturn("dummyAccessToken");
        when(jwtUtils.generateRefreshTokenFromAccountId(String.valueOf(1L)))
                .thenReturn("dummyRefreshToken");

        // Mock access UserProfile to get fullname
        UserProfile dummyProfile = new UserProfile();
        dummyProfile.setFullName("Test User");
        when(userProfileRepository.findById(String.valueOf(1L))).thenReturn(Optional.of(dummyProfile));


        // Call tested method
        ResponseEntity<ApiResponse<?>> responseEntity = authenticationService.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ApiResponse<LoginResponse> loginResponse = (ApiResponse<LoginResponse>) responseEntity.getBody();
        assertEquals(ERole.CUSTOMER.toString(), loginResponse.getData().getUserRole());
        assertEquals("Test User", loginResponse.getData().getFullName());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateAccessTokenFromUserEmail("user@example.com");
        verify(jwtUtils, times(1)).generateRefreshTokenFromAccountId(String.valueOf(1L));
        verify(userProfileRepository, times(1)).findById(String.valueOf(1L));

        // is the header cookies set with share variable
        List<String> setCookieHeaders = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeaders);
        //  sendApiResponseResponseEntity has 4 header SET_COOKIE
        assertEquals(4, setCookieHeaders.size());
        // check cookies' name contains "accessToken" and "refreshToken"
        boolean hasAccessTokenCookie = setCookieHeaders.stream().anyMatch(cookie -> cookie.contains("accessToken"));
        boolean hasRefreshTokenCookie = setCookieHeaders.stream().anyMatch(cookie -> cookie.contains("refreshToken"));
        assertTrue(hasAccessTokenCookie);
        assertTrue(hasRefreshTokenCookie);
    }

    @Test
    void login_withInvalidCredentials_shouldThrowAppException() {
        // Given wrong email, password
        LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongPassword");

        // mock calling authenticate throw BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // assert throw to check whether the exception thrown correctly
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals(ErrorCode.INVALID_LOGIN_INFORMATION, exception.getErrorCode());
    }

    @Test
    void testRefreshToken_Successful() {
        // GIven
        String refreshToken = "validRefreshToken";
        String accountId = "123";
        String userEmail = "test@example.com";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setEmail(userEmail);
        mockAccount.setActive(true);

        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{
                new jakarta.servlet.http.Cookie("refreshToken", refreshToken)
        });
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenReturn(true);
        when(invalidateRefreshTokenRepo.findByToken(refreshToken)).thenReturn(Optional.empty());

        when(jwtUtils.getUserAccountIdFromRefreshToken(refreshToken)).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(jwtUtils.generateAccessTokenFromUserEmail(userEmail)).thenReturn(newAccessToken);
        when(jwtUtils.generateRefreshTokenFromAccountId(accountId)).thenReturn(newRefreshToken);

        // Calling
        ResponseEntity<ApiResponse<?>> response = authenticationService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully refresh token", response.getBody().getData());

        // old refresh token should  be saved to db
        verify(invalidateRefreshTokenRepo, times(1)).save(any(InvalidateRefreshToken.class));
    }

    @Test
    void testRefreshToken_Fail_NoToken() {
        when(request.getCookies()).thenReturn(null);

        //assert throw exceptiion ìf no token in cookie
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.refreshToken(request);
        });

        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }

    @Test
    void testRefreshToken_Fail_CannotValidateToken() {
        String refreshToken = "invalidatedToken";

        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{
                new jakarta.servlet.http.Cookie("refreshToken", refreshToken)
        });

        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenThrow(new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED));

        //assert
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.refreshToken(request);
        });

        assertEquals(ErrorCode.REFRESH_TOKEN_EXPIRED, exception.getErrorCode());
    }

    @Test
    void testRefreshToken_Fail_InvalidatedToken() {
        String refreshToken = "invalidatedToken";

        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{
                new jakarta.servlet.http.Cookie("refreshToken", refreshToken)
        });
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenReturn(true);
        when(invalidateRefreshTokenRepo.findByToken(refreshToken)).thenReturn(Optional.of(new InvalidateRefreshToken()));

        // Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.refreshToken(request);
        });

        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }

    @Test
    void testRefreshToken_Fail_AccountNotFound() {
        String refreshToken = "validToken";
        String accountId = "nonExistingAccount";

        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{
                new jakarta.servlet.http.Cookie("refreshToken", refreshToken)
        });
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenReturn(true);
        when(invalidateRefreshTokenRepo.findByToken(refreshToken)).thenReturn(Optional.empty());
        when(jwtUtils.getUserAccountIdFromRefreshToken(refreshToken)).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.refreshToken(request);
        });

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    @Test
    void testRefreshToken_Fail_AccountInactive() {
        String refreshToken = "validToken";
        String accountId = "123";
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setActive(false);

        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{
                new jakarta.servlet.http.Cookie("refreshToken", refreshToken)
        });
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenReturn(true);
        when(invalidateRefreshTokenRepo.findByToken(refreshToken)).thenReturn(Optional.empty());
        when(jwtUtils.getUserAccountIdFromRefreshToken(refreshToken)).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.refreshToken(request);
        });

        assertEquals(ErrorCode.ACCOUNT_IS_INACTIVE, exception.getErrorCode());
    }

    @Test
    void testLogout_WithValidTokens() {
        // Given
        String accessToken = "validAccessToken";
        String refreshToken = "validRefreshToken";

        Cookie cookieAccess = new Cookie("accessToken", accessToken);
        Cookie cookieRefresh = new Cookie("refreshToken", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{ cookieAccess, cookieRefresh });

        // mock valid not throw exception
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenReturn(true);
        Date refreshExpiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        when(jwtUtils.getExpirationDateFromRefreshToken(refreshToken)).thenReturn(refreshExpiration);

        when(jwtUtils.validateJwtAccessToken(accessToken)).thenReturn(true);
        Date accessExpiration = new Date(System.currentTimeMillis() + 1800 * 1000);
        when(jwtUtils.getExpirationDateFromAccessToken(accessToken)).thenReturn(accessExpiration);

        // logout
        ResponseEntity<ApiResponse<?>> response = authenticationService.logout(request);

        // verify invalidateRefreshTokenRepo.save called
        verify(invalidateRefreshTokenRepo, times(1))
                .save(argThat(token ->
                        refreshToken.equals(token.getToken()) &&
                                refreshExpiration.equals(token.getExpiresAt())
                ));

        verify(invalidateAccessTokenRepo, times(1))
                .save(argThat(token ->
                        accessToken.equals(token.getToken()) &&
                                accessExpiration.equals(token.getExpiresAt())
                ));

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testLogout_WithInvalidRefreshToken() {
        //Given
        String accessToken = "validAccessToken";
        String refreshToken = "invalidRefreshToken";

        Cookie cookieAccess = new Cookie("accessToken", accessToken);
        Cookie cookieRefresh = new Cookie("refreshToken", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{ cookieAccess, cookieRefresh });

        // Mock invalid refresh token
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenThrow(new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED));

        // Mock valid access token
        when(jwtUtils.validateJwtAccessToken(accessToken)).thenReturn(true);
        Date accessExpiration = new Date(System.currentTimeMillis() + 1800 * 1000);
        when(jwtUtils.getExpirationDateFromAccessToken(accessToken)).thenReturn(accessExpiration);

        // Gọi logout
        ResponseEntity<ApiResponse<?>> response = authenticationService.logout(request);

        // verify not saving invalidate cho refresh token
        verify(invalidateRefreshTokenRepo, never()).save(any());

        // saving access token to invalidated Access token
        verify(invalidateAccessTokenRepo, times(1)).save(any());

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }


    @Test
    void testLogout_WithInvalidAccessToken() {
        //Given
        String accessToken = "validAccessToken";
        String refreshToken = "invalidRefreshToken";

        Cookie cookieAccess = new Cookie("accessToken", accessToken);
        Cookie cookieRefresh = new Cookie("refreshToken", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{ cookieAccess, cookieRefresh });

        // Mock valid refresh token
        when(jwtUtils.validateJwtRefreshToken(refreshToken)).thenReturn(true);
        Date refreshExpiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        when(jwtUtils.getExpirationDateFromRefreshToken(refreshToken)).thenReturn(refreshExpiration);

        // Mock INvalid access token
        when(jwtUtils.validateJwtAccessToken(accessToken)).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        // Gọi logout
        ResponseEntity<ApiResponse<?>> response = authenticationService.logout(request);

        // saving access token to invalidated Access token
        verify(invalidateRefreshTokenRepo, times(1)).save(any());

        // verify not saving invalidate cho refresh token
        verify(invalidateAccessTokenRepo, never()).save(any());


        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testLogout_NoTokens() {
        //Given tokens in cookies is null
        when(request.getCookies()).thenReturn(null);

        ResponseEntity<ApiResponse<?>> response = authenticationService.logout(request);

        // no saving
        verify(invalidateRefreshTokenRepo, never()).save(any());
        verify(invalidateAccessTokenRepo, never()).save(any());

        // assert response
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

}