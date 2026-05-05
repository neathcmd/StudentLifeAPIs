package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Order(2)
@SuppressWarnings("null")
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.admin.username}")
    private String adminUsername;

    @Value("${spring.admin.email}")
    private String adminEmail;

    @Value("${spring.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("Seeding...");

        seedAdmin();

        log.info("Seeding completed");
    }

    private void seedAdmin() {

        // Prevent duplicate admin
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin already exist.");
            return;
        }

        Roles adminRole = roleRepository.findByName("admin")
                .orElseThrow(() -> {
                    log.info("Failed to seed admin!");
                    return notFound("Role admin not found.");
                });

        Users admin = Users.builder()
                .fullname("Student Life Admin")
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("096 482 731")
                .roles(new HashSet<>(Set.of(adminRole)))
                .isActive(true)
                .build();

        userRepository.save(admin);
        log.info("Admin seeded successfully");
    }
}