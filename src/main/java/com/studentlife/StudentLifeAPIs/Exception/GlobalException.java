package com.studentlife.StudentLifeAPIs.Exception;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

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

    // =========================================
    // DEV + PROD: Validation errors (@Valid DTO)
    // =========================================
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        // DEV: return first validation message (simple & clear)
        String message = Objects.requireNonNull(
                ex.getBindingResult().getFieldError()
        ).getDefaultMessage();

        // PROD (OPTIONAL): return generic validation error
        // String message = "Invalid request data";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        400,
                        false,
                        message,
                        null
                ));
    }

    // =========================================
    // PROD SAFETY NET
    // DEV: helps catch bugs early
    // PROD: hides internal details
    // =========================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnexpected(Exception ex) {

        // DEV ONLY (OPTIONAL): log stack trace
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        500,
                        false,
                        "Internal server error",
                        null
                ));
    }
}
