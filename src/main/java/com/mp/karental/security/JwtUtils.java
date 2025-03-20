package com.mp.karental.security;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Utils class, perform operation relate to JWT
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Component
public class JwtUtils {

    @Value("${application.security.jwt.access-token-expiration}")
    @NonFinal
    private long accessTokenExpiration;

    @Value("${application.security.jwt.access-token-secret-key}")
    @NonFinal
    private String accessTokenSecretKey;

    @Value("${application.security.jwt.refresh-token-expiration}")
    @NonFinal
    private long refreshTokenExpiration;

    @Value("${application.security.jwt.refresh-token-secret-key}")
    @NonFinal
    private String refreshTokenSecretKey;

    /**
     * get the jwt SecretKey from secret key in environment variable
     * @return SecretKey object which is used in generate and decode token
     */
    private SecretKey getSecretKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ==================================================================================
     * Generate jwt
     */

    /**
     * Generate token from user's email
     * @param email
     * @return
     */
    public String generateAccessTokenFromUserEmail(String email) {
        return generateJwtToken(email, accessTokenSecretKey, accessTokenExpiration);
    }

    public String generateRefreshTokenFromAccountId(String accountId){
        return generateJwtToken(accountId, refreshTokenSecretKey, refreshTokenExpiration);
    }

    private String generateJwtToken(String email, String secretKey, long expiration){
        return Jwts.builder()
                .subject(email)
                .issuer("${spring.application.name}")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration)) //set expiration date for token
                .signWith(getSecretKey(secretKey)) //the algorithm is automatically determine by the api of jjwt
                .compact();
    }


    /**
     * ==================================================================================
     * Validate jwt
     */
    /**
     * Validate the access token
     * @param accessToken
     * @return
     */
    public boolean validateJwtAccessToken(String accessToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey(accessTokenSecretKey))
                    .build()
                    .parse(accessToken);
            return true;
        } catch (MalformedJwtException e) {
            //Invalid JWT token
            throw new  AppException(ErrorCode.UNAUTHENTICATED);
        } catch (ExpiredJwtException e) {
            //JWT token is expired
            throw new  AppException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        } catch (IllegalArgumentException e) {
            //JWT claims string is empty
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Validate the refreshToken
     * @param refreshToken
     * @return
     */
    public boolean validateJwtRefreshToken(String refreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey(refreshTokenSecretKey))
                    .build()
                    .parse(refreshToken);
            return true;
        } catch (ExpiredJwtException e) {
            //JWT token is expired
            throw new  AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        } catch (IllegalArgumentException e) {
            //JWT claims string is empty
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * ==================================================================================
     * Get claim from jwt
     */

    /**
     * decode the token then get the email from the claim "sub"
     * @param accessToken
     * @return
     */
    public String getUserEmailFromAccessToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey(accessTokenSecretKey))
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();
    }

    public String getUserAccountIdFromRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey(refreshTokenSecretKey))
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getSubject();
    }

    public Instant getExpirationAtFromAccessToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey(accessTokenSecretKey))
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getExpiration()
                .toInstant();
    }

    public Instant getExpirationAtFromRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey(refreshTokenSecretKey))
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getExpiration()
                .toInstant();
    }
}
