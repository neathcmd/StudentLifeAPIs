package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateUserRolesRequest;
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

    // ─── PATCH /api/v1/users/{id} ────────────────────────────────────────────
    @PatchMapping("/{id}")
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

    // ─── PATCH /api/v1/users/{id}/roles ─────────────────────────────────────
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        UserResponse updated = userService.updateUserRoles(id, request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                true,
                "User roles updated successfully.",
                updated
        ));
    }

    // ─── PATCH /api/v1/users/{id}/disable ───────────────────────────────────
    @PatchMapping("/{id}/disable")
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