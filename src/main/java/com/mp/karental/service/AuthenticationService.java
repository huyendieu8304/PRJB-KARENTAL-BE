package com.mp.karental.service;

import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.AuthenticationResponse;
import com.mp.karental.entity.RefreshToken;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.security.jwt.JwtUtils;
import com.mp.karental.security.service.RefreshTokenService;
import com.mp.karental.security.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    AuthenticationManager authenticationManager;
    JwtUtils jwtUtils;
    RefreshTokenService refreshTokenService;

    public ResponseEntity<?> login(LoginRequest request){
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );

        //set Authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //generate cookies
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        //Generate access token cookie
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        //Generate refresh token cookie
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getAccoutnId());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateJwtRefreshCookie(refreshToken.getToken());

        //TODO: phải nhét role vào Response
        String role = userDetails.getRole().getName().toString();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString()) //return access token to cookies
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()) //return refresh token to cookies
                .body(
                        ApiResponse.<AuthenticationResponse>builder().data(new AuthenticationResponse(role)).build()
                );

    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        //get the refresh token out from cookies
        String refreshToken = jwtUtils.getJwtRefreshFromCookie(request);

        //refresh token exist in the cookies
        if(refreshToken != null && !refreshToken.isEmpty()) {
            return refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getAccount)
                    .map(account -> {
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(account);

                        ApiResponse apiResponse = ApiResponse.<String>builder()
                                .data("Successfully refresh token")
                                .build();
                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .body(apiResponse);
                    })
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN)); //refresh token not exist in db
        } else {
            //refresh token is empty
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }


    //get loggin user's details
//    UserDetails userDetails =
//            (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

// userDetails.getUsername()
// userDetails.getPassword()
// userDetails.getAuthorities()


}
