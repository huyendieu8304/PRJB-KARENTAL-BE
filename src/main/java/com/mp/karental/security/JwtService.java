package com.mp.karental.security;

import com.mp.karental.entity.Account;
import com.mp.karental.entity.Token;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {

    @Value("${application.security.jwt.access-token-expiration}")
    @NonFinal
    private long accessTokenExpiration;
    @Value("${application.security.jwt.refresh-token-expiration}")
    @NonFinal
    private long refreshTokenExpiration;


    SecretKey getSignedKey; //in Security config


    public String generateAccessToken(Account account) {
        return generateToken(account, accessTokenExpiration);
    }

    public String generateRefreshToken(Account account) {
        return generateToken(account, refreshTokenExpiration);
    }

    /**
     * Generate token
     * @param account the user's account
     * @param expirationTime the duration indicate how long the generate token valid
     * @return a String - token
     */
    private String generateToken(Account account, long expirationTime) {
        return Jwts.builder()
                .subject(account.getEmail())
                .issuer("${spring.application.name}")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) //set expiration date for token
                .claim("role", account.getRole().getName()) //adding custom claim to jwt
                .signWith(getSignedKey) //the algorithm is automatically determine by the api of jjwt
                .compact();
    }

}
