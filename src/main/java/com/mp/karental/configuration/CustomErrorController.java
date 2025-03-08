package com.mp.karental.configuration;

import com.mp.karental.dto.response.ApiResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {
    @GetMapping
    public ResponseEntity<ApiResponse<String>> handleError(HttpServletRequest request) {
        log.info("Handling error in CustomErrorController");
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            log.info("Error Code: {}", statusCode);
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                        .code(4999)
                        .message("Resource not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(apiResponse);
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<String>builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("An unexpected error occurred")
                        .build());
    }
}
