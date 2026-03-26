package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

import static com.studentlife.StudentLifeAPIs.Enum.ScheduleType.ONE_TIME;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class ScheduleSeeder implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding...");

        Users student = seedStudent();
        seedSchedule("Math 101",       "Calculus lecture",      1, "08:00", "09:30", "Room A1",  student);
        seedSchedule("Physics Lab",    "Weekly lab session",    2, "10:00", "12:00", "Lab B2",   student);
        seedSchedule("English 201",    "Writing and grammar",   3, "13:00", "14:30", "Room C3",  student);
        seedSchedule("CS Algorithms",  "Data structures class", 4, "09:00", "10:30", "Room D4",  student);
        seedSchedule("History",        "Modern history",        5, "14:00", "15:30", "Room E5",  student);

        log.info("Seed completed.");
    }

    private Users seedStudent() {
        return userRepository.findByUsername("student_demo").orElseGet(() -> {
            Roles studentRole = roleRepository.findByName("student")
                    .orElseThrow(() -> new RuntimeException("Role 'student' not found. Run RoleSeeder first."));

            Users user = Users.builder()
                    .fullname("Demo Student")
                    .username("student_demo")
                    .email("student@demo.com")
                    .password(passwordEncoder.encode("password123"))
                    .university("Demo University")
                    .major("Computer Science")
                    .academicYear("Year 2")
                    .roles(Set.of(studentRole))
                    .build();

            log.info("Demo student created.");
            return userRepository.save(user);
        });
    }

    private void seedSchedule(String title, String description, int dayOfWeek,
                              String startTime, String endTime, String location, Users user) {
        if (scheduleRepository.existsByTitleAndUser(title, user)) {
            log.info("Schedule '{}' already exists, skipping.", title);
            return;
        }

        scheduleRepository.save(
                Schedules.builder()
                        .title(title)
                        .description(description)
                        .type(ONE_TIME)
                        .dayOfWeek(dayOfWeek)
                        .startTime(OffsetDateTime.parse("2025-01-06T" + startTime + ":00Z").toLocalDateTime())
                        .endTime(OffsetDateTime.parse("2025-01-06T" + endTime + ":00Z").toLocalDateTime())
                        .location(location)
                        .user(user)
                        .build()
        );

        log.info("Schedule '{}' seeded successfully.", title);
    }
}