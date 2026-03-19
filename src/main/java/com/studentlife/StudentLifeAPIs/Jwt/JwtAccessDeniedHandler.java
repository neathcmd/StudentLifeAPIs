package com.studentlife.StudentLifeAPIs.Jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<String> body = new ApiResponse<>(403, false, "You do not have permission to access this resource", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
