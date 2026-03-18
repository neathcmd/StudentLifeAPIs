package com.studentlife.StudentLifeAPIs.Exception;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    // =========================================
    // DEV + PROD: Custom API exception
    // Used for controlled business errors
    // =========================================
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<String>> handleApiException(ApiException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(
                        ex.getStatus(),
                        false,
                        ex.getMessage(),
                        null
                ));
    }
}
