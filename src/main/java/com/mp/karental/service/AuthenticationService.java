package com.mp.karental.service;

import com.mp.karental.dto.request.LoginRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.AuthenticationResponse;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.security.jwt.JwtUtils;
import com.mp.karental.security.service.UserDetailsImpl;
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

    public ResponseEntity<?> login(LoginRequest request){
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        //TODO: phải nhét role vào Response
        String role = userDetails.getRole().getName().toString();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().data(new AuthenticationResponse(role)).build());

    }



    //get loggin user's details
//    UserDetails userDetails =
//            (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

// userDetails.getUsername()
// userDetails.getPassword()
// userDetails.getAuthorities()


}
