package com.mp.karental.security.auth;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.service.UserDetailsServiceImpl;
import com.mp.karental.security.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Arrays;


/**
 * Authentication filter
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    @Value("${application.security.jwt.access-token-cookie-name}")
    @NonFinal
    private String accessTokenCookieName;

    @Value("${application.security.public-endpoints}")
    @NonFinal
    private String[] publicEndpoints;

    JwtUtils jwtUtils;
    UserDetailsServiceImpl userDetailsService;
    TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws
            ServletException, IOException {

        String uri = request.getRequestURI();
        log.info("{} go to AuthTokenFilter", uri);

        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        //skip authentication with public endpoints
        PathPatternParser pathPatternParser = new PathPatternParser();
        if (Arrays
                .stream(publicEndpoints)
                .anyMatch(endpoint -> pathPatternParser.parse(endpoint)
                        .matches(PathContainer.parsePath(path)))) {
            log.info("Public endpoint: {}, skipping authentication", path);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        Cookie cookie = WebUtils.getCookie(request, accessTokenCookieName);
        if (cookie != null) {
            jwt = cookie.getValue();
            log.info("cookie found: {}", jwt);
        } else {
            log.info("Missing access token cookie");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        //validate jwt
        jwtUtils.validateJwtAccessToken(jwt);

        //token still valid -> check whether it invalidated (by logout)
        if(tokenService.isAccessTokenInvalidated(jwt)){
            //access token is invalidated
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // JWT is valid
        //get the email of the user to
        String email = jwtUtils.getUserEmailFromAccessToken(jwt);

        //Load UserDetails (the information of authenticated user)
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails,
                        null, //authenticated request, so that doesn't need password
                        userDetails.getAuthorities());

        //save the information's of request to authentication object to used later
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        //set up UserDetails in current SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //continue filter chain
        filterChain.doFilter(request, response);
    }

}
