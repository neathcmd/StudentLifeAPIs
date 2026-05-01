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
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class ExtraUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Seeding extra peer students...");

        Roles studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> new RuntimeException("Role 'student' not found."));

        List<UserSeed> peers = List.of(
                new UserSeed("Dara Sopheak",   "@dara",    "dara.sopheak@gmail.com"),
                new UserSeed("Vanna Bopha",    "@bopha",   "vanna.bopha@gmail.com"),
                new UserSeed("Chenda Ratana",  "@chenda",  "chenda.ratana@gmail.com"),
                new UserSeed("Piseth Kosal",   "@piseth",  "piseth.kosal@gmail.com"),
                new UserSeed("Sreyleak Mony",  "@srey",    "sreyleak.mony@gmail.com"),
                new UserSeed("Makara Vibol",   "@makara",  "makara.vibol@gmail.com"),
                new UserSeed("Leakhena Soda",  "@leakhena","leakhena.soda@gmail.com"),
                new UserSeed("Bunthoeun Rith", "@rith",    "bunthoeun.rith@gmail.com")
        );

        for (UserSeed seed : peers) {
            if (userRepository.existsByEmail(seed.email())) {
                log.info("Peer '{}' already exists, skipping.", seed.email());
                continue;
            }

            Users user = Users.builder()
                    .fullname(seed.fullname())
                    .username(seed.username())
                    .email(seed.email())
                    .password(passwordEncoder.encode("password123"))
                    .roles(new HashSet<>(Set.of(studentRole)))
                    .isActive(true)
                    .build();

            userRepository.save(user);
            log.info("Peer '{}' seeded.", seed.fullname());
        }

        log.info("Extra peer students seeding completed.");
    }

    private record UserSeed(String fullname, String username, String email) {}
}