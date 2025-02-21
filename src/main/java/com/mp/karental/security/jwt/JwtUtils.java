package com.mp.karental.security.jwt;

import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${application.security.jwt.cookieName}")
    @NonFinal
    private String cookieName;

    @Value("${application.security.jwt.secret-key}")
    @NonFinal
    private String jwtSecretKey;

    @Value("${application.security.jwt.access-token-expiration}")
    @NonFinal
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token-expiration}")
    @NonFinal
    private long refreshTokenExpiration;

    /**
     * Get the cookie out from the request
     * @param request
     * @return
     */
    public String getJwtFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }


    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUserEmail(userPrincipal.getEmail());
        //TODO: sửa lại dòng này, đang fix cứng domain
        return ResponseCookie
                .from(cookieName, jwt)
                .path("/karental")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();

    }

//    public ResponseCookie getCleanJwtCookie() {
//        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/api").build();
//        return cookie;
//    }

    public String getUserEmailFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * get the jwt SecretKey from secret key in environment variable
     * @return SecretKey object which is used in generate and decode token
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }



    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parse(authToken);
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

    public String generateTokenFromUserEmail(String email) {
        return Jwts.builder()
                .subject(email)
                .issuer("${spring.application.name}")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) //set expiration date for token
                .signWith(key()) //the algorithm is automatically determine by the api of jjwt
                .compact();
    }



}
