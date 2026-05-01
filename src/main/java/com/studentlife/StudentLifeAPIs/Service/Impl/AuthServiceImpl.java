package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.AuthRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.*;
//import com.studentlife.StudentLifeAPIs.Entity.RefreshToken;
import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Jwt.JwtService;
import com.studentlife.StudentLifeAPIs.Mapper.UserMapper;
import com.studentlife.StudentLifeAPIs.Repository.RefreshTokenRepository;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.AuthService;
//import com.studentlife.StudentLifeAPIs.Utils.CookieUtil;
import com.studentlife.StudentLifeAPIs.Utils.UserValidatorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//import java.util.UUID;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
//    private final CookieUtil cookieUtil;
    private final UserValidatorUtil userValidatorUtil;
    private final AuthenticationManager authenticationManager;

//    @Override
//    public ApiResponse<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
//
//        // ========================
//        // GET REFRESH TOKEN FROM COOKIE
//        // ========================
//        String refreshTokenValue = cookieUtil.getCookieValue(request, "refreshToken");
//
//        if (refreshTokenValue == null) {
//            throw unauthorized("Refresh token is missing.");
//        }
//
//        // ========================
//        // LOOK UP REFRESH TOKEN IN DATABASE
//        // ========================
//        RefreshToken oldToken = refreshTokenRepository
//                .findByToken(refreshTokenValue)
//                .orElseThrow(() -> unauthorized("Invalid refresh token."));
//
//        // ========================
//        // VALIDATE REFRESH TOKEN
//        // - Must not be revoked
//        // - Must not be expired
//        // ========================
//        if (oldToken.isRevoked() || oldToken.getExpiresAt().isBefore(Instant.now())) {
//            throw unauthorized("Request failed. Refresh token already expired!");
//        }
//
//        // ========================
//        // EXTRACT USER FROM TOKEN
//        // ========================
//        Users user = oldToken.getUsers();
//
//        // ========================
//        // DELETE THE OLD TOKEN AFTER GENERATE A NEW TOKEN
//        // ========================
//        refreshTokenRepository.delete(oldToken);
//
//        // ========================
//        // GENERATE NEW ACCESS TOKEN (JWT)
//        // ========================
//        String newAccessToken = jwtService.generateAccessToken(
//                String.valueOf(user.getId()),
//                user.getEmail(),
//                user.getUsername(),
//                user.getRoles()
//                        .stream()
//                        .map(Roles::getName)
//                        .toList()
//        );
//
//        // ========================
//        // CREATE NEW REFRESH TOKEN (SERVER-SIDE)
//        // ========================
//        String newRefreshToken = UUID.randomUUID().toString();
//
//        RefreshToken newToken = new RefreshToken();
//        newToken.setToken(newRefreshToken);
//        newToken.setUsers(user);
//        newToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
//
//        refreshTokenRepository.save(newToken);
//
//        // ========================
//        // STORE NEW TOKENS IN HTTP-ONLY COOKIES
//        // ========================
//        cookieUtil.setAccessTokenCookie(response, newAccessToken);
//        cookieUtil.setRefreshTokenCookie(response, newRefreshToken);
//
//        return new ApiResponse<>(
//                201,
//                true,
//                "New access token generate successfully.",
//                new RefreshTokenResponse(newAccessToken)
//        );
//    }

    @Override
    public ApiResponse<?> register(
            RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()
                || userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw validation("This email or username already been used.");
        }

        // username email validation
        userValidatorUtil.validateRegister(request);

        Users user = userMapper.toUserEntityRegisterUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ========================
        // ASSIGN ROLES TO USER
        // ========================
        Roles defaultRole = roleRepository.findByName("student")
                .orElseThrow(() -> notFound("Default role not found."));
        user.setRoles(new HashSet<>(Set.of(defaultRole)));

        Users savedUser = userRepository.save(user);

        // ========================
        // EXTRACT ROLE NAMES
        // ========================
        List<String> roles = user.getRoles()
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
//        String refreshTokenValue = UUID.randomUUID().toString();

//        RefreshToken refreshToken = new RefreshToken();
//        refreshToken.setToken(refreshTokenValue);
//        refreshToken.setUsers(user);
//        refreshToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
//
//        refreshTokenRepository.save(refreshToken);

        // ========================
        // SAVE TOKENS IN COOKIE
        // ========================
//        cookieUtil.setAccessTokenCookie(response, accessToken);
//        cookieUtil.setRefreshTokenCookie(response, refreshTokenValue);

        // ========================
        // MAP USER TO RESPONSE USING MAPSTRUCT
        // ========================
        UserResponse userResponse = userMapper.toUserResponse(savedUser);

        return new ApiResponse<>(
                201,
                true,
                "Register successful.",
                new AuthResponse(accessToken, userResponse)
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> login(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {

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
        // EXTRACT ROLE NAME FROM AN EXISTING USER
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
//        refreshTokenRepository.deleteByUsers(user);
//        String refreshTokenValue = UUID.randomUUID().toString();
//
//        RefreshToken refreshToken = new RefreshToken();
//        refreshToken.setToken(refreshTokenValue);
//        refreshToken.setUsers(user);
//        refreshToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
//
//        refreshTokenRepository.save(refreshToken);

        // ========================
        // SAVE TOKENS IN COOKIE
        // ========================
//        cookieUtil.setAccessTokenCookie(response, accessToken);
//        cookieUtil.setRefreshTokenCookie(response, refreshTokenValue);

        return new ApiResponse<>(
                200,
                true,
                "Login successful",
                new AuthResponse(accessToken, userResponse)
        );
    }

//    @Override
//    @Transactional
//    public ApiResponse<Object> logout(HttpServletRequest request, HttpServletResponse response) {
//
//        String refreshTokenValue = cookieUtil.getCookieValue(request, "refreshToken");
//
//        if (refreshTokenValue != null) {
//            refreshTokenRepository.findByToken(refreshTokenValue)
//                    .ifPresent(refreshTokenRepository::delete);
//        }
//
//        cookieUtil.clearAuthCookie(response, "accessToken");
//        cookieUtil.clearAuthCookie(response, "refreshToken");
//
//        return new ApiResponse<>(
//                200,
//                true,
//                "User logout successfully."
//        );
//    }
}