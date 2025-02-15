package com.mp.karental.configuration;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * This class is responsible for security configuration in the application
 *
 * @author DieuTTH4
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
     * @author DieuTTH4
     * @version 1.0
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        request -> request
                                //open public endpoints
                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                                .anyRequest().authenticated()
                ).cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("*")); //danh sách các origin được phép truy cập, các origin cách nhau bởi dấu ","
                        config.setAllowedMethods(Collections.singletonList("*")); //cho phép các http method được truy cập. VD chỉ muốn cho phép các method get từ origin đã cho, nếu để dấu * là cho phép tất cả methods
                        config.setAllowCredentials(true); //true là cho phép truyền credentials hoặc cookie khi gọi backend API
                        config.setAllowedHeaders(Collections.singletonList("*")); //danh sách các headers mà backend chấp nhận từ UI app hoặc các origin khác. Dấu * là cho phép tất cả các loại request headers
                        config.setExposedHeaders(Arrays.asList("Authorization"));
                        config.setMaxAge(3600L); //cấu hình này sẽ tồn tại trong bao lâu (đơn vị second). Sau khi truy cập lần đầu tiên thì các lần sau sẽ không cần phải xét lại miễn là còn thời gian
                        return config;
                    }
                }))

                .csrf(AbstractHttpConfigurer::disable);


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
     * @author DieuTTH4
     * @version 1.0
     */
//    @Bean
//    public CorsFilter corsFilter() {
//
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.addAllowedOrigin("http://localhost:3000"); //Allow this app to be accessed from localhost:3000
//        corsConfiguration.addAllowedMethod("*"); //allow all method
//        corsConfiguration.addAllowedHeader("*"); //allow all header
//
//        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//        //apply CORS configuration for all endpoints in the application
//        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
//
//        return new CorsFilter(urlBasedCorsConfigurationSource);
//    }

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
