package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProfileInfo() {
        return ResponseEntity.status(200).body(userService.GetProfileInfo());
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<ApiResponse<?>> updatedUserProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.status(200).body(userService.updateUserProfile(request));
    }
}
