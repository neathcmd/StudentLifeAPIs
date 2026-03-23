package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.AuthRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AuthResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.RegisterResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Jwt.JwtService;
import com.studentlife.StudentLifeAPIs.Mapper.UserMapper;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.AuthService;
import com.studentlife.StudentLifeAPIs.Utils.CookieUtil;
import com.studentlife.StudentLifeAPIs.Utils.UserValidatorUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final CookieUtil cookieUtil;
    private final UserValidatorUtil userValidatorUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public ApiResponse<?> register(RegisterRequest request, HttpServletResponse response) {

        Users user = userRepository.findByEmail(request.getEmail())
                .or(() -> userRepository.findByUsername(request.getUsername()))
                .orElseThrow(() -> validation("This email or username already been used. Please try another email or username"));

        // username email validation
        userValidatorUtil.validateRegister(request);

        userMapper.toUserEntityRegisterUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ========================
        // ASSIGN ROLES TO USER
        // ========================
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Roles> roles = roleRepository.findAllById(request.getRoles());
            if (roles.size() != request.getRoles().size()) {
                throw badRequest("Some roles not found.");
            }
            user.setRoles(new HashSet<>(roles));
        } else {
            Roles defaultRole = roleRepository.findByName("user")
                    .orElseThrow(() -> notFound("Default role not found."));
            user.setRoles(new HashSet<>(Set.of(defaultRole)));
        }

        Users savedUser = userRepository.save(user);

        // ========================
        // EXTRACT ROLE NAMES
        // ========================
        // List<String> roles = mapperFunction.mapRolesToStringList(savedUser.getRoles());
        List<String> roles = user.getRoles()
//        savedUser.getRoles()
                .stream()
                .map(Roles::getName)
                .toList();

        // ========================
        // GENERATE TOKENS
        // ========================
        String accessToken = jwtService.generateAccessToken(
                String.valueOf(savedUser.getId()),
                savedUser.getEmail(),
                savedUser.getUsername(),
                roles
        );

        // ========================
        // GENERATE REFRESH TOKEN
        // ========================
        String refreshTokenValue = jwtService.generateRefreshToken(
                String.valueOf(user.getId())
        );

        // ========================
        // SAVE TOKENS IN COOKIE
        // ========================
        cookieUtil.setAuthCookie(
                response,
                "accessToken",
                accessToken,
                900 // 15 minutes
        );

        cookieUtil.setAuthCookie(
                response,
                "refreshToken",
                refreshTokenValue,
                259200 // 30 days
        );

        // ========================
        // MAP USER TO RESPONSE USING MAPSTRUCT
        // ========================
        UserResponse userResponse = userMapper.toUserResponse(savedUser);

        return new ApiResponse<>(
                201,
                true,
                "Register successful.",
                new RegisterResponse(userResponse)
        );
    }

    @Override
    public ApiResponse<?> login(AuthRequest request, HttpServletResponse response) {

        // ========================
        // VALIDATE USER CREDENTIAL WITH AUTHENTICATION MANAGER
        // ========================
       try {
           Authentication authentication = authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(
                           request.getEmail_or_username(),
                           request.getPassword()
                   )
           );
       } catch (AuthenticationException e) {
           throw validation("Login failed. Please check your credentials.");
       }

        // ========================
        // Find user by check both email or username
        // ========================
        Users user = userRepository.findByEmail(request.getEmail_or_username())
                .or(() -> userRepository.findByUsername(request.getEmail_or_username()))
                .orElseThrow(() -> notFound("User not exist."));


        // ========================
        // EXTRACT ROLE NAME FROM AN EXISTING USER (Whoever was trying to Log in extract their role name then display it in the response.)
        // ========================
        List<String> roles = user.getRoles()
                .stream()
                .map(Roles::getName)
                .toList();

        // ========================
        // MAP USER TO RESPONSE USING MAPSTRUCT
        // ========================
        UserResponse userResponse = userMapper.toUserResponse(user);

        // ========================
        // GENERATE ACCESS TOKEN
        // ========================
        String accessToken = jwtService.generateAccessToken(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getUsername(),
                roles
        );

        // ========================
        // GENERATE REFRESH TOKEN
        // ========================
        String refreshTokenValue = jwtService.generateRefreshToken(
                String.valueOf(user.getId())
        );

        // ========================
        // SAVE TOKENS IN COOKIE
        // ========================
        cookieUtil.setAuthCookie(
                response,
                "accessToken",
                accessToken,
                300 // 5 minutes
        );

        cookieUtil.setAuthCookie(
                response,
                "refreshToken",
                refreshTokenValue,
                259200 // 30 days
        );

        return new ApiResponse<>(
                200,
                true,
                "Login successful",
                new AuthResponse(accessToken, userResponse)
        );
    }
}
