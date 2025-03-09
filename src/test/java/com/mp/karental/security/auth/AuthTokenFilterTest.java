package com.mp.karental.security.auth;

import com.mp.karental.constant.ERole;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.entity.UserDetailsImpl;
import com.mp.karental.security.service.UserDetailsServiceImpl;
import com.mp.karental.security.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthTokenFilter
 *
 * @author DieuTTH4
 *
 * @version 1.1
 */
@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {
    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private final String accessTokenCookieName = "accessToken";
    private final String[] PUBLIC_ENDPOINTS = new String[]
            {
                    "/public", "/login"
            };

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authTokenFilter, "accessTokenCookieName", accessTokenCookieName);
//        ReflectionTestUtils.setField(SecurityConfig.class, "PUBLIC_ENDPOINTS", new String[]{"/customPublic", "/anotherPublic"});
        SecurityContextHolder.clearContext();
    }

    //TODO: viết lại test sau khi tìm được phương án public endpoint phù hợp
//    @Test
//    void testDoFilterInternal_PublicEndpoint() throws ServletException, IOException {
//        // Given: the request URI (minus context path) exactly equals one of the custom public endpoints.
//        when(request.getRequestURI()).thenReturn("/customPublic");
//        when(request.getContextPath()).thenReturn("");
//
//        // When: the filter's doFilterInternal method is invoked
//        authTokenFilter.doFilterInternal(request, response, filterChain);
//
//        // Then: the filter should simply pass the request along the filter chain without performing authentication.
//        verify(filterChain, times(1)).doFilter(request, response);
//        // And: no authentication should be set in the SecurityContext
//        assertNull(SecurityContextHolder.getContext().getAuthentication(),
//                "No authentication should be set for requests to public endpoints");
//    }

    @Test
    void testDoFilterInternal_NoJwtCookie() {
        // Given:
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // When & Then: The filter should throw an AppException with UNAUTHENTICATED error code
        AppException exception = assertThrows(AppException.class, () ->
                authTokenFilter.doFilterInternal(request, response, filterChain)
        );
        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    /**
     * Test that the filter throws an AppException with UNAUTHENTICATED error code
     * when the provided JWT has been invalidated.
     */
    @Test
    void testDoFilterInternal_TokenInvalidated() {
        // Given: A secure endpoint with a valid JWT cookie is provided
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");
        Cookie jwtCookie = new Cookie(accessTokenCookieName, "validJwtToken");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        // Simulate successful JWT validation
        when(jwtUtils.validateJwtAccessToken("validJwtToken")).thenReturn(true);
        // Simulate that the token is found in the invalidated tokens
        when(tokenService.isAccessTokenInvalidated("validJwtToken")).thenReturn(true);

        // When & Then: The filter should throw an AppException indicating unauthenticated access
        AppException exception = assertThrows(AppException.class, () ->
                authTokenFilter.doFilterInternal(request, response, filterChain)
        );
        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    /**
     * Test that the filter processes a valid JWT correctly.
     * This includes validating the token, loading user details, setting the authentication in the security context,
     * and continuing the filter chain.
     */
    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        // Given: A secure endpoint with a valid JWT cookie is provided
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");
        Cookie jwtCookie = new Cookie(accessTokenCookieName, "validJwtToken");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        // Simulate successful JWT validation and that the token is not invalidated
        when(jwtUtils.validateJwtAccessToken("validJwtToken")).thenReturn(true);
        when(tokenService.isAccessTokenInvalidated("validJwtToken")).thenReturn(false);

        // Simulate extracting the user's email from the token
        when(jwtUtils.getUserEmailFromAccessToken("validJwtToken")).thenReturn("user@example.com");

        // Simulate loading the UserDetails based on the email
        Account mockAccount = Account.builder()
                .email("user@example.com")
                .role(Role.builder().name(ERole.CUSTOMER).build())
                .build();
        UserDetails userDetails = UserDetailsImpl.build(mockAccount);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);

        // When: The filter is executed
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then: The SecurityContext should be populated with the authentication object
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set in the SecurityContext");
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getName(), "Username should match the one extracted from the token");
        // And: The filter chain should have been continued
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        // Given: A secure endpoint with a valid JWT cookie is provided
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");
        Cookie jwtCookie = new Cookie(accessTokenCookieName, "invalidJwtToken");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        // Simulate unsucessful JWT validation
        when(jwtUtils.validateJwtAccessToken("invalidJwtToken")).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        // When: The filter is executed
        assertThrows(AppException.class, () -> {
            authTokenFilter.doFilterInternal(request, response, filterChain);
        });


        //invalidateAccessToken  is not called
        verify(tokenService, times(0)).isAccessTokenInvalidated("invalidJwtToken");
        // And: The filter chain should have not continued
        verify(filterChain, times(0)).doFilter(request, response);
    }


}