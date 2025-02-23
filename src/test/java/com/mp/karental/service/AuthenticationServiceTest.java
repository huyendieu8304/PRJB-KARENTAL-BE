package com.mp.karental.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.LoginResponse;
import com.mp.karental.entity.Role;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.entity.UserDetailsImpl;
import com.mp.karental.security.repository.InvalidateAccessTokenRepo;
import com.mp.karental.security.repository.InvalidateRefreshTokenRepo;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

}