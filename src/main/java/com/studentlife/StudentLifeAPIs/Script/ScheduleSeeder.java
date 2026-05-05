package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(4)
@SuppressWarnings("null")
public class ScheduleSeeder implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    private static final String TARGET_EMAIL = "salmonineath31@gmail.com";

    @Override
    public void run(String... args) {
        log.info("Seeding schedules...");

        Users user = userRepository.findByEmail(TARGET_EMAIL)
                .orElseThrow(() -> new RuntimeException("Target user not found. Run UserSeeder first."));

        if (scheduleRepository.existsByUser(user)) {
            log.info("Schedules already seeded for this user, skipping.");
            return;
        }

        List<Schedules> schedules = List.of(

                // ── ONE_TIME (5) ──────────────────────────────────────────────────────

                Schedules.builder()
                        .title("Mid-Term Exam — Software Engineering")
                        .description("Covers chapters 1–6, held in Building A, Room 201")
                        .type(ScheduleType.ONE_TIME)
                        .startTime(LocalDateTime.now().plusDays(3).withHour(8).withMinute(0).withSecond(0).withNano(0))
                        .endTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0))
                        .location("Building A, Room 201")
                        .isImportant(true)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Group Project Presentation")
                        .description("Present the UI/UX prototype to the lecturer")
                        .type(ScheduleType.ONE_TIME)
                        .startTime(LocalDateTime.now().plusDays(7).withHour(13).withMinute(0).withSecond(0).withNano(0))
                        .endTime(LocalDateTime.now().plusDays(7).withHour(14).withMinute(30).withSecond(0).withNano(0))
                        .location("Lab 3, ICT Building")
                        .isImportant(true)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Library Study Session")
                        .description("Prepare notes for the final exam")
                        .type(ScheduleType.ONE_TIME)
                        .startTime(LocalDateTime.now().plusDays(10).withHour(9).withMinute(0).withSecond(0).withNano(0))
                        .endTime(LocalDateTime.now().plusDays(10).withHour(12).withMinute(0).withSecond(0).withNano(0))
                        .location("University Library, 2nd Floor")
                        .isImportant(false)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Internship Interview")
                        .description("Technical interview at TechCorp Phnom Penh")
                        .type(ScheduleType.ONE_TIME)
                        .startTime(LocalDateTime.now().plusDays(14).withHour(10).withMinute(0).withSecond(0).withNano(0))
                        .endTime(LocalDateTime.now().plusDays(14).withHour(11).withMinute(0).withSecond(0).withNano(0))
                        .location("TechCorp, Phnom Penh")
                        .isImportant(true)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Advisor Meeting")
                        .description("Discuss thesis progress with academic advisor")
                        .type(ScheduleType.ONE_TIME)
                        .startTime(LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).withSecond(0).withNano(0))
                        .endTime(LocalDateTime.now().plusDays(5).withHour(16).withMinute(0).withSecond(0).withNano(0))
                        .location("Faculty Office, Room 105")
                        .isImportant(false)
                        .user(user)
                        .build(),

                // ── RECURRING (5) ─────────────────────────────────────────────────────
                // dayOfWeek: 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat

                Schedules.builder()
                        .title("Web Development Class")
                        .description("Weekly lecture on React and Spring Boot integration")
                        .type(ScheduleType.RECURRING)
                        .dayOfWeek(1) // Monday
                        .recurringStartTime(LocalTime.of(8, 0))
                        .recurringEndTime(LocalTime.of(10, 0))
                        .location("Room 302, ICT Building")
                        .isImportant(false)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Database Systems Class")
                        .description("Weekly lecture covering SQL, indexing, and transactions")
                        .type(ScheduleType.RECURRING)
                        .dayOfWeek(3) // Wednesday
                        .recurringStartTime(LocalTime.of(10, 0))
                        .recurringEndTime(LocalTime.of(12, 0))
                        .location("Room 101, Science Block")
                        .isImportant(false)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Morning Gym Session")
                        .description("Workout routine to stay active during semester")
                        .type(ScheduleType.RECURRING)
                        .dayOfWeek(2) // Tuesday
                        .recurringStartTime(LocalTime.of(6, 0))
                        .recurringEndTime(LocalTime.of(7, 0))
                        .location("University Sports Center")
                        .isImportant(false)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("Study Group — Algorithms")
                        .description("Weekly peer study session on data structures and algorithms")
                        .type(ScheduleType.RECURRING)
                        .dayOfWeek(4) // Thursday
                        .recurringStartTime(LocalTime.of(14, 0))
                        .recurringEndTime(LocalTime.of(16, 0))
                        .location("Library Meeting Room B")
                        .isImportant(true)
                        .user(user)
                        .build(),

                Schedules.builder()
                        .title("English Communication Class")
                        .description("Weekly class focusing on academic writing and presentations")
                        .type(ScheduleType.RECURRING)
                        .dayOfWeek(5) // Friday
                        .recurringStartTime(LocalTime.of(13, 0))
                        .recurringEndTime(LocalTime.of(15, 0))
                        .location("Language Lab, Room 204")
                        .isImportant(false)
                        .user(user)
                        .build()
        );

        scheduleRepository.saveAll(schedules);
        log.info("10 schedules seeded successfully for user '{}'.", TARGET_EMAIL);
    }
}