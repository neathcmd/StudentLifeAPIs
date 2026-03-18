package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {

    private int status;
    private Boolean success;
    private String message;
    private T data;

    public ApiResponse(int status, Boolean success, String message) {
        this.status = status;
        this.success = success;
        this.message = message;
    }

    public ApiResponse(int status, Boolean success, String message, T data) {
        this.status = status;
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
