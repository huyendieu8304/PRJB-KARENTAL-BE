package com.mp.karental.configuration;

import com.mp.karental.security.auth.AuthEntryPointJwt;
import com.mp.karental.security.auth.AuthTokenFilter;
import com.mp.karental.security.auth.CustomAccessDeniedHandler;
import com.mp.karental.security.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.*;

/**
 * This class is responsible for security configuration in the application
 *
 * @author DieuTTH4
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig{


    /**
     * Define public endpoints, the endpoint that could be accessed without needing to provide any authentication header
     */
    //TODO: tìm cách khác để define cái public enpoints này, dùng static không ổn lắm
    public static final String[] PUBLIC_ENDPOINTS = {
            "/user/register",
            "/user/check-unique-email",
            "/auth/login",
            "/auth/logout",
            "/auth/refresh-token"
    };
    /**
     * Allow request from other origins below
     */
    private final List<String> ALLOWED_CORS_URL = List.of(new String[]{
            "http://localhost:3000" //TODO: replace this with the endpoint of deployed front end
    });


    UserDetailsServiceImpl userDetailsService;
    AuthEntryPointJwt jwtAuthenticationEntryPoint;
    AuthTokenFilter authTokenFilter;


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(ALLOWED_CORS_URL);
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setExposedHeaders(Collections.singletonList("Authorization"));
                        config.setAllowCredentials(true);
                        return config;
                    }
                }))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)) //unauthorized request
                .exceptionHandling(e -> e.accessDeniedHandler(new CustomAccessDeniedHandler()))//unauthorized access
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //make each request independently
                .authorizeHttpRequests( //authorization in http url
                        request -> request
                                //open public endpoints
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                //endpoints for user has role CAR_OWNER
                                .requestMatchers("/car-owner/**").hasRole("CAR_OWNER")
                                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                                .anyRequest().authenticated()
                );
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
//        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }


    /**
     * Configures the password encoder for the application.
     * <p>
     * This method sets up a BCryptPasswordEncoder with a strength of 10,
     * which is used to hash user passwords for secure storage.
     * </p>
     *
     * @return A PasswordEncoder that uses BCrypt hashing algorithm.
     * @author DieuTTH4
     * @version 1.0
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }



}
