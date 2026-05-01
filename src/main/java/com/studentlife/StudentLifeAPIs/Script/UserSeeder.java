package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Seeding student user...");

        seedStudent();

        log.info("Student user seeding completed.");
    }

    private void seedStudent() {
        final String email = "salmonineath31@gmail.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Student user already exists, skipping.");
            return;
        }

        Roles studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> {
                    log.error("Failed to seed student user — 'student' role not found!");
                    return notFound("Role 'student' not found.");
                });

        Users student = Users.builder()
                .fullname("Sal Monineath")
                .username("@neath")
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .roles(new HashSet<>(Set.of(studentRole)))
                .isActive(true)
                .build();

        userRepository.save(student);
        log.info("Student user 'Sal Monineath' seeded successfully.");
    }
}