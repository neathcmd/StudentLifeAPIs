package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OneSignalController {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerPlayerId(@RequestBody Map<String, String> body) {
        String playerId = body.get("playerIed");
        if (playerId == null || playerId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Users user = authUtil.getAuthenticatedUser();
        user.setOneSignalPlayerId(playerId);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Player ID registered."));
    }
}
