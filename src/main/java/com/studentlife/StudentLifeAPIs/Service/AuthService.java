package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.AuthRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    ApiResponse<?> register(RegisterRequest request, HttpServletResponse response);

    ApiResponse<?> login(AuthRequest request, HttpServletResponse response);

}
