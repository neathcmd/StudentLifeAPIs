package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding...");

        seedRole("admin");
        seedRole("student");

        log.info("Seed completed.");
    }

    private void seedRole(String name) {
        if (roleRepository.existsByName(name)) {
            log.info("Role already exist skip.");
            return;
        }

        roleRepository.save(
                Roles.builder()
                        .name(name)
                        .build()
        );

        log.info("Role seed successfully.");
    }
}
