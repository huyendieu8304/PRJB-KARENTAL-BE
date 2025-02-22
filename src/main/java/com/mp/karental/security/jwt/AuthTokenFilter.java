package com.mp.karental.security.jwt;

import com.mp.karental.configuration.SecurityConfig;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    UserDetailsServiceImpl userDetailsService;


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
            System.out.println(path+" "+publicEndpoint);
            if(path.equals(publicEndpoint)){
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            System.out.println("HELLO");
            //get jwt from the HTTP Cookies
            String jwt = jwtUtils.getJwtFromCookie(request);

            // Validate JWT
            if (jwt == null || !jwtUtils.validateJwtToken(jwt)) {
                // If JWT invalid, continue filter without process Authentication
                filterChain.doFilter(request, response);
                return;
            }

            //request has JWT  and JWT is valid
            //get the email of the user to
            String email = jwtUtils.getUserEmailFromJwtToken(jwt);

            //Load UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails,
                            null,
                            userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //set up UserDetails in current SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // token expired
            throw new AppException(ErrorCode.UNABLE_TO_SET_USER_AUTHENTICATION);
        }
        //continue filter chain
        filterChain.doFilter(request, response);
    }

}
