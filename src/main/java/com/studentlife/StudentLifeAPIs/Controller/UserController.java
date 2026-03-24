package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateUserRolesRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import com.studentlife.StudentLifeAPIs.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── GET /api/v1/users?page=0&size=10&sort=createdAt,desc ───────────────
    @GetMapping()
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<UserResponse> paginatedUsers = userService.getAllUsers(page, size);
        return new ApiResponse<>(
                200,
                true,
                "Get users successfully.",
                paginatedUsers
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('admin")
    public ResponseEntity<ApiResponse<UserResponse>> createdUser(
            @RequestBody UserCreateRequest request
            ) {
        UserResponse created = userService.createUser(request);

        return ResponseEntity.ok((new ApiResponse<>(
                HttpStatus.CREATED.value(),
                true,
                "User created successfully.",
                created
        )));
    }

    // ─── GET /api/v1/users/{id} ──────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                true,
                "User retrieved successfully.",
                user
        ));
    }

    // ─── PUT /api/v1/users/{id} ────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin') or @userSecurity.isSelf(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse updated = userService.updateUser(id, request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                true,
                "User updated successfully.",
                updated
        ));
    }

    // ─── PUT /api/v1/users/{id}/disable ───────────────────────────────────
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        userService.disableUser(id);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                true,
                "User disabled successfully."
        ));
    }

    // ─── DELETE /api/v1/users/{id} ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                true,
                "User permanently deleted."
        ));
    }
}