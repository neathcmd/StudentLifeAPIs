package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.AuthRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    ApiResponse<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    );

    ApiResponse<?> register(
            RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    );

    ApiResponse<?> login(
            AuthRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    );

    ApiResponse<Object> logout(
            HttpServletRequest request,
            HttpServletResponse response
    );

}
