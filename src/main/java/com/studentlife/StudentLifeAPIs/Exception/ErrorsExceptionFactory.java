package com.studentlife.StudentLifeAPIs.Exception;

import org.springframework.http.HttpStatus;

import java.io.IOException;

// ======= ERROR FACTORY ======= //
public final class ErrorsExceptionFactory {

    private ErrorsExceptionFactory() { }

    public static ApiException badRequest(String message) {
        return new ApiException(400, message != null ? message : "Bad request.");
    }

    public static ApiException notFound(String message) {
        return new ApiException(
                HttpStatus.NOT_FOUND.value(),
                message != null ? message : "Data not found."
        );
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(401, message != null ? message : "Token required.");
    }

    public static ApiException forbidden(String message) {
        return new ApiException(403, message != null ? message : "You are not allowed to do this action.");
    }

    public static ApiException validation(String message) {
        return new ApiException(422, message != null ? message : "Validation failed.");
    }

    public static ApiException internal(String message, IOException e) {
        return new ApiException(500, message != null ? message : "Internal server error.");
    }
}
