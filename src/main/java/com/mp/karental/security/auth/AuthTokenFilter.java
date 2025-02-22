package com.mp.karental.security.auth;

import com.mp.karental.configuration.SecurityConfig;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.JwtUtils;
import com.mp.karental.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Value("${application.security.jwt.access-token-cookie-name}")
    @NonFinal
    private String accessTokenCookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws
            ServletException, IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        //Skip authentication with public endpoints
        for(String publicEndpoint : SecurityConfig.PUBLIC_ENDPOINTS){
            if(path.equals(publicEndpoint)){
                filterChain.doFilter(request, response);
                return;
            }
        }

        String jwt = null;
        try {
            //get jwt from the HTTP Cookies
            jwt = WebUtils.getCookie(request, accessTokenCookieName).getValue();
            //validate jwt
            jwtUtils.validateJwtAccessToken(jwt);

            //TODO: kiêmr tra xem cái token này nó có nằm trong token bị invalidated do logout hay không

            // JWT is valid
            //get the email of the user to
            String email = jwtUtils.getUserEmailFromAccessToken(jwt);

            //Load UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails,
                            null,
                            userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //set up UserDetails in current SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (NullPointerException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        //continue filter chain
        filterChain.doFilter(request, response);
    }

}
