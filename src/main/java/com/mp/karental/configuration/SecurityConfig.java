package com.mp.karental.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * This class is responsible for security configuration in the application
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    /**
     * Define public endpoints, the endpoint that could be accessed without needing to provide any authentication header
     */
    private String[] PUBLIC_ENDPOINTS = {
            "/user/register"
    };

    /**
     * Configures the security filter chain for the application.
     * <p>
     * This method sets up security rules for HTTP requests:
     * <ul>
     *     <li>Allows public access to specific POST endpoints defined in {@code PUBLIC_ENDPOINTS}.</li>
     *     <li>Requires authentication for all other requests.</li>
     *     <li>Disables CSRF protection.</li>
     * </ul>
     * </p>
     *
     * @param http The HttpSecurity object used to configure security for HTTP requests.
     * @return A SecurityFilterChain that defines the application's security rules.
     * @throws Exception If an error occurs during configuration.
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                request -> request
                        //open public endpoints
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
        ).csrf(AbstractHttpConfigurer :: disable);

        return http.build();
    }

    /**
     * Config the CORS filter for the application
     *
     * <p>
     * This method set up CORS configuration, allow request from {@code http://localhost:3000} in all methods
     * for all endpoint of the application
     * </p>
     *
     * @return {@code CorsFilter} allow request from localhost:3000
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:3000"); //Allow this app to be accessed from localhost:3000
        corsConfiguration.addAllowedMethod("*"); //allow all method
        corsConfiguration.addAllowedHeader("*"); //allow all header

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        //apply CORS configuration for all endpoints in the application
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    /**
     * Configures the password encoder for the application.
     * <p>
     * This method sets up a BCryptPasswordEncoder with a strength of 10,
     * which is used to hash user passwords for secure storage.
     * </p>
     *
     * @return A PasswordEncoder that uses BCrypt hashing algorithm.
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

}
