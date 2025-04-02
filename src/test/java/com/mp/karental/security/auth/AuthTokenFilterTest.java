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
    private final String csrfTokenHeaderName = "X-CSRF-Token";
    private final String[] PUBLIC_ENDPOINTS = new String[]
            {
                    "/public", "/login"
            };

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authTokenFilter, "accessTokenCookieName", accessTokenCookieName);
        ReflectionTestUtils.setField(authTokenFilter, "csrfTokenHeaderName", csrfTokenHeaderName);
        ReflectionTestUtils.setField(authTokenFilter, "publicEndpoints", PUBLIC_ENDPOINTS);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_PublicEndpoint() throws ServletException, IOException {
        // Given: the request URI (minus context path) exactly equals one of the custom public endpoints.
        when(request.getRequestURI()).thenReturn("/public");
        when(request.getContextPath()).thenReturn("");

        // When: the filter's doFilterInternal method is invoked
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then: the filter should simply pass the request along the filter chain without performing authentication.
        verify(filterChain, times(1)).doFilter(request, response);
        // And: no authentication should be set in the SecurityContext
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "No authentication should be set for requests to public endpoints");
    }



    @Test
    void testDoFilterInternal_NoCSRFToken() throws ServletException, IOException {

        String accessToken = "validJwtToken";

        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");

        //get access token from cookie
        Cookie jwtCookie = new Cookie(accessTokenCookieName, accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        when(request.getHeader(csrfTokenHeaderName)).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("ERROR_CODE", ErrorCode.INVALID_CSRF_TOKEN.toString());
    }

    @Test
    void testDoFilterInternal_InvalidCSRFToken() throws ServletException, IOException {

        String accessToken = "validJwtToken";

        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");
        when(request.getHeader(csrfTokenHeaderName)).thenReturn("invalidCsrfToken");

        //get access token from cookie
        Cookie jwtCookie = new Cookie(accessTokenCookieName, accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtils.validateJwtAccessToken(accessToken)).thenReturn(true);

        when(jwtUtils.validateJwtCsrfToken("invalidCsrfToken")).thenThrow(new AppException(ErrorCode.INVALID_CSRF_TOKEN));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("ERROR_CODE", ErrorCode.INVALID_CSRF_TOKEN.toString());
    }

    @Test
    void testDoFilterInternal_NoJwtCookie() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");


        when(request.getCookies()).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("ERROR_CODE", ErrorCode.UNAUTHENTICATED.toString());
    }

    @Test
    void testDoFilterInternal_ValidAccess() throws ServletException, IOException {
        String accessToken = "validJwtToken";
        String csrfToken = "csrfToken";

        //the endpoint is not public
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");

        //get csrf token from header
        when(request.getHeader(csrfTokenHeaderName)).thenReturn(csrfToken);
        when(jwtUtils.validateJwtCsrfToken(csrfToken)).thenReturn(true);

        //get access token from cookie
        Cookie jwtCookie = new Cookie(accessTokenCookieName, accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtils.validateJwtAccessToken(accessToken)).thenReturn(true);

        //email in the csrf token is same as access token
        String sameEmail =  "user@example.com";
        when(jwtUtils.getUserEmailFromAccessToken(accessToken)).thenReturn(sameEmail);
        when(jwtUtils.getUserEmailFromCsrfToken(csrfToken)).thenReturn(sameEmail);

        //Access token and csrf token is not invalidate
        when(tokenService.isAccessTokenInvalidated(accessToken)).thenReturn(false);
        when(tokenService.isCsrfTokenInvalidated(csrfToken)).thenReturn(false);

        // Simulate loading the UserDetails based on the email
        Account mockAccount = Account.builder()
                .email(sameEmail)
                .role(Role.builder().name(ERole.CUSTOMER).build())
                .build();
        UserDetails userDetails = UserDetailsImpl.build(mockAccount);
        when(userDetailsService.loadUserByUsername(sameEmail)).thenReturn(userDetails);

        // The filter is executed
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // The SecurityContext should be populated with the authentication object
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set in the SecurityContext");
        assertEquals(sameEmail, SecurityContextHolder.getContext().getAuthentication().getName(), "Username should match the one extracted from the token");
        // The filter chain should have been continued
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidatedAccessToken() throws ServletException, IOException {
        String accessToken = "validJwtToken";
        String csrfToken = "csrfToken";

        //the endpoint is not public
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");

        //get csrf token from header
        when(request.getHeader(csrfTokenHeaderName)).thenReturn(csrfToken);
        when(jwtUtils.validateJwtCsrfToken(csrfToken)).thenReturn(true);

        //get access token from cookie
        Cookie jwtCookie = new Cookie(accessTokenCookieName, accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtils.validateJwtAccessToken(accessToken)).thenReturn(true);

        //email in the csrf token is same as access token
        String sameEmail =  "user@example.com";
        when(jwtUtils.getUserEmailFromAccessToken(accessToken)).thenReturn(sameEmail);
        when(jwtUtils.getUserEmailFromCsrfToken(csrfToken)).thenReturn(sameEmail);

        when(tokenService.isAccessTokenInvalidated(accessToken)).thenReturn(true);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("ERROR_CODE", ErrorCode.UNAUTHENTICATED.toString());
    }

    @Test
    void testDoFilterInternal_InvalidatedCsrfToken() throws ServletException, IOException {
        String accessToken = "validJwtToken";
        String csrfToken = "csrfToken";

        //the endpoint is not public
        when(request.getRequestURI()).thenReturn("/secure");
        when(request.getContextPath()).thenReturn("");

        //get csrf token from header
        when(request.getHeader(csrfTokenHeaderName)).thenReturn(csrfToken);
        when(jwtUtils.validateJwtCsrfToken(csrfToken)).thenReturn(true);

        //get access token from cookie
        Cookie jwtCookie = new Cookie(accessTokenCookieName, accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtils.validateJwtAccessToken(accessToken)).thenReturn(true);

        //email in the csrf token is same as access token
        String sameEmail =  "user@example.com";
        when(jwtUtils.getUserEmailFromAccessToken(accessToken)).thenReturn(sameEmail);
        when(jwtUtils.getUserEmailFromCsrfToken(csrfToken)).thenReturn(sameEmail);

        when(tokenService.isAccessTokenInvalidated(accessToken)).thenReturn(false);
        when(tokenService.isCsrfTokenInvalidated(csrfToken)).thenReturn(true);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("ERROR_CODE", ErrorCode.INVALID_CSRF_TOKEN.toString());
    }


}