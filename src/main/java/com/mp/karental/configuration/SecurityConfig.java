package com.mp.karental.configuration;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
public class SecurityConfig {

    @Value("${application.security.jwt.secret-key}")
    @NonFinal
    private String jwtSecretKey;

    /**
     * Define public endpoints, the endpoint that could be accessed without needing to provide any authentication header
     */
    private final String[] PUBLIC_ENDPOINTS = {
            "/user/register",
            "/auth/login"
    };

//    /**
//     * Define endpoints that only CAR_OWNER could access
//     */
//    private final String[] CAR_OWNER_ONLY_ENDPOINTS = {
////            "/user/register",
//            "/auth/login"
//    };
//
//    private final String[] CUSTOMER_ONLY_ENDPOINTS = {
//            "/auth/login"
//    };
//
//    private final String[] OPERATOR_ONLY_ENDPOINTS = {
//            "/auth/login"
//    };


    /**
     * Allow request from other origins below
     */
    private final List<String> ALLOWED_CORS_URL = List.of(new String[]{
            "http://localhost:3000" //TODO: replace this with the endpoint of deployed front end
    });



    /**
     * Configures the application's security filter chain using Spring Security.
     *
     * <p>This method sets up the following security configurations:
     * <ul>
     *   <li><strong>CSRF:</strong> CSRF protection is disabled.</li>
     *   <li><strong>CORS:</strong> A custom CORS configuration is applied that:
     *     <ul>
     *       <li>Allows origins specified in {@code ALLOWED_CORS_URL}.</li>
     *       <li>Permits all HTTP methods and headers.</li>
     *       <li>Exposes the "Authorization" header.</li>
     *       <li>Supports credentials.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Authorization:</strong> Endpoint access is configured such that:
     *     <ul>
     *       <li>Public endpoints defined by {@code PUBLIC_ENDPOINTS} (for POST requests) are accessible without authentication.</li>
     *       <li>Endpoints under {@code "/car-owner/**"} require the user to have the "CAR_OWNER" role.</li>
     *       <li>Endpoints under {@code "/customer/**"} require the user to have the "CUSTOMER" role.</li>
     *       <li>All other requests require authentication.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Exception Handling:</strong> A custom access denied handler ({@link CustomAccessDeniedHandler})
     *       is set to handle unauthorized access attempts.</li>
     *   <li><strong>OAuth2 Resource Server:</strong> The resource server is configured to use JWT-based authentication:
     *     <ul>
     *       <li>A custom JWT decoder ({@link #customJwtDecoder()}) is used to validate and decode tokens.</li>
     *       <li>A custom JWT authentication converter ({@link #customJwtAuthenticationConverter()}) maps JWT claims to authorities.</li>
     *       <li>A custom authentication entry point ({@link JwtAuthenticationEntryPoint}) handles cases where the token is invalid or missing.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p>The fully configured {@link SecurityFilterChain} is returned to enforce these security policies.
     *
     * @param http the {@link HttpSecurity} to modify.
     * @return a {@link SecurityFilterChain} that encapsulates the configured security settings.
     * @throws Exception if an error occurs while configuring the security settings.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
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
                .authorizeHttpRequests(
                        request -> request
                                //open public endpoints
                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                                //endpoints for user has role CAR_OWNER
//                                .requestMatchers("/car-owner/**").hasRole("CAR_OWNER")
//                                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                                .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.accessDeniedHandler(new CustomAccessDeniedHandler())) //unauthorized access
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(jwtConfigurer -> jwtConfigurer
                                        .decoder(customJwtDecoder())
                                        .jwtAuthenticationConverter(customJwtAuthenticationConverter())
                            ).authenticationEntryPoint(new JwtAuthenticationEntryPoint()) //token invalid or not provide in header
                )
                .build();
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


    /**
     * get the jwt SecretKey from secret key in environment variable
     * @return SecretKey object which is used in generate and decode token
     */
    @Bean
    public SecretKey getSignedKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Creates a custom {@link JwtDecoder} to manually decode and validate JWT tokens.
     *
     * <p>This decoder performs the following actions:
     * <ul>
     *     <li>Parses the token using a signed key to extract all claims.</li>
     *     <li>Retrieves the issued-at (`iat`) and expiration (`exp`) timestamps.</li>
     *     <li>Removes these timestamps from the claims map to prevent unnecessary exposure.</li>
     *     <li>Creates a simple JWT header with the algorithm set to "HS256".</li>
     *     <li>Constructs a new {@link Jwt} object containing the parsed claims, headers, and timestamps.</li>
     * </ul>
     *
     * <p>If the token is expired, it throws a {@link BadCredentialsException} with a custom
     * {@link AppException} indicating that the access token has expired. If the token is otherwise
     * invalid, it throws a similar exception indicating authentication failure.
     *
     * @return a {@link JwtDecoder} that decodes and validates JWT tokens.
     * @throws BadCredentialsException if the token is expired or invalid.
     */
    @Bean
    public JwtDecoder customJwtDecoder(){
        return token -> {
            try {
                //get all claims in the token
                Claims claims = Jwts.parser()
                        .verifyWith(getSignedKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                //get issueAt and expiration time in claims
                Date issueAt = claims.getIssuedAt();
                Date expiration = claims.getExpiration();
                //convert Claims to Map
                Map<String, Object> claimsMap = new HashMap<>(claims);
                //remove issue at and expiration claim from claims map
                claimsMap.remove("iat");
                claimsMap.remove("exp");

                //create simple header for Jwt object
                Map<String, Object> headers = new HashMap<>();
                headers.put("alg", "HS256");

                return Jwt.withTokenValue(token)
                        .headers(h -> h.putAll(headers))
                        .issuedAt(issueAt.toInstant())
                        .expiresAt(expiration.toInstant())
                        .claims(c -> c.putAll(claimsMap))
                        .build();

            } catch (ExpiredJwtException e){
                //the token is expired
                //wrap the AppException into AuthenticationException
                throw new BadCredentialsException("The access token is expired. Please try again", new AppException(ErrorCode.ACCESS_TOKEN_EXPIRED));
            } catch (JwtException e){
                throw new BadCredentialsException("Token is invalid", new AppException(ErrorCode.UNAUTHENTICATED));
            }
        };
    }

    /**
     * Configures a custom {@link JwtAuthenticationConverter} to extract user roles from a custom claim.
     *
     * <p>This method sets up a {@link JwtGrantedAuthoritiesConverter} to map the "role" claim
     * from the JWT token into Spring Security authorities. The following configurations are applied:
     * <ul>
     *     <li>Authorities are prefixed with "ROLE_" to align with Spring Security's role conventions.</li>
     *     <li>The "role" claim is used as the source for user authorities.</li>
     * </ul>
     *
     * <p>The configured {@link JwtAuthenticationConverter} is then returned, ensuring that
     * authentication tokens correctly map user roles for authorization checks in the application.
     *
     * @return a customized {@link JwtAuthenticationConverter} instance.
     */
    @Bean
    JwtAuthenticationConverter customJwtAuthenticationConverter(){
        //use JwtGrantedAuthoritiesConverter to map claim "role" (my custom claim - watch in generateToken)
        //get custom claim "role" in claim set to make it into Authority
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
