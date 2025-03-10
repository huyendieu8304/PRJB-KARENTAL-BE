package com.mp.karental.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler the case that the provided token is invalid or not provided in the header
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {


        log.info("go to AuthEntryPointJwt commence");
        log.info(authException.getMessage());
        log.info(authException.getLocalizedMessage());

        // Is request response with 404
        if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(4999)
                    .message("Resource not found")
                    .build();

            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            response.getWriter().flush();
            return;
        }

        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED; //by default, it would be ErrorCode.UNAUTHENTICATED

        //Iterate through cause to found AppException
        Throwable cause = authException;
        while (cause != null) {
            if (cause instanceof AppException) {
                errorCode = ((AppException) cause).getErrorCode();
                break;
            }
            cause = cause.getCause();
        }

        response.setStatus(errorCode.getHttpStatusCode().value()); //Set the http status code
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //set header content type
        //Set the body of the response
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        ObjectMapper objectMapper = new ObjectMapper(); //map ApiResponse to string to put it in HttpServletResponse
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
