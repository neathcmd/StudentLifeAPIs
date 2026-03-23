package com.studentlife.StudentLifeAPIs.Utils;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.badRequest;

@Component
@RequiredArgsConstructor
public class UserValidatorUtil {

    private final UserRepository userRepository;

    // =================
    // EMAIL FORMAT VALIDATOR
    // =================
    public void validateEmailFormat(String email) {
        if (email == null || !email.contains("@") || !email.endsWith(".com")) {
            throw badRequest("Email must contain '@' and end with '.com'");
        }
    }

    // =================
    // CREATE VALIDATION
    // =================
    public void validateCreate(UserCreateRequest request) {
        validateEmailFormat(request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw badRequest("Email is already in use.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw badRequest("Username is already in use.");
        }
    }

    // =================
    // UPDATE VALIDATION (PROFILE ONLY)
    // =================
    public void validateUpdate(UserUpdateRequest request) {

        if (request.getFullname() == null || request.getFullname().isBlank()) {
            throw badRequest("Full name must not be empty.");
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw badRequest("Phone number must not be empty.");
        }

    }

    public void validateRegister(RegisterRequest request) {
        // Validate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw badRequest("Username already exists");
        }

        // Validate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw badRequest("Email already exists");
        }

        validateEmailFormat(request.getEmail());
    }
}
