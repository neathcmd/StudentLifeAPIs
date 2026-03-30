package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Roles;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import com.studentlife.StudentLifeAPIs.Repository.RoleRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

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
    public void run(String... args) {
        log.info("Seeding schedules...");

        Users student = seedStudent();

        // ── RECURRING schedules (weekly classes) ──────────────────────────────
        // dayOfWeek: 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat
        seedRecurring("Math 101",      "Calculus lecture",       1, "08:00", "09:30", "Room A1", false, student);
        seedRecurring("Physics Lab",   "Weekly lab session",     2, "10:00", "12:00", "Lab B2",  false, student);
        seedRecurring("English 201",   "Writing and grammar",    3, "13:00", "14:30", "Room C3", false, student);
        seedRecurring("CS Algorithms", "Data structures class",  4, "09:00", "10:30", "Room D4", false, student);
        seedRecurring("History",       "Modern history",         5, "14:00", "15:30", "Room E5", false, student);

        // ── ONE_TIME schedules (specific dated events) ────────────────────────
        seedOneTime("Midterm Exam",    "Math midterm exam",      "2026-04-10", "09:00", "11:00", "Hall A",    true,  student);
        seedOneTime("Group Study",     "Study session for CS",   "2026-04-05", "14:00", "17:00", "Library",   false, student);
        seedOneTime("Advisor Meeting", "Semester course review", "2026-04-03", "11:00", "11:30", "Office C1", true,  student);

        log.info("Schedule seeding completed.");
    }

    // ── Seed helpers ──────────────────────────────────────────────────────────

    private void seedRecurring(String title, String description, int dayOfWeek,
                               String startTime, String endTime, String location,
                               boolean isImportant, Users user) {
        if (scheduleRepository.existsByTitleAndUserId(title, user.getId())) {
            log.info("Schedule '{}' already exists, skipping.", title);
            return;
        }

        scheduleRepository.save(
                Schedules.builder()
                        .title(title)
                        .description(description)
                        .type(ScheduleType.RECURRING)
                        .dayOfWeek(dayOfWeek)
                        .recurringStartTime(LocalTime.parse(startTime))
                        .recurringEndTime(LocalTime.parse(endTime))
                        .location(location)
                        .isImportant(isImportant)
                        .user(user)
                        .build()
        );

        log.info("Recurring schedule '{}' seeded.", title);
    }

    private void seedOneTime(String title, String description, String date,
                             String startTime, String endTime, String location,
                             boolean isImportant, Users user) {
        if (scheduleRepository.existsByTitleAndUserId(title, user.getId())) {
            log.info("Schedule '{}' already exists, skipping.", title);
            return;
        }

        LocalDate localDate = LocalDate.parse(date);

        scheduleRepository.save(
                Schedules.builder()
                        .title(title)
                        .description(description)
                        .type(ScheduleType.ONE_TIME)
                        .startTime(LocalDateTime.of(localDate, LocalTime.parse(startTime)))
                        .endTime(LocalDateTime.of(localDate, LocalTime.parse(endTime)))
                        .location(location)
                        .isImportant(isImportant)
                        .user(user)
                        .build()
        );

        log.info("One-time schedule '{}' seeded.", title);
    }

    // ── User helper ───────────────────────────────────────────────────────────

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
}