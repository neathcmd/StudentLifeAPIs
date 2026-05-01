package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(5)
public class AssignmentSeeder implements CommandLineRunner {

    private final AssignmentRepository assignmentRepository;
    private final ScheduleRepository   scheduleRepository;
    private final UserRepository       userRepository;

    private static final String TARGET_EMAIL = "salmonineath31@gmail.com";

    @Override
    public void run(String... args) {
        log.info("Seeding assignments...");

        Users user = userRepository.findByEmail(TARGET_EMAIL)
                .orElseThrow(() -> new RuntimeException("Target user not found. Run UserSeeder first."));

        if (assignmentRepository.existsByUser(user)) {
            log.info("Assignments already seeded for this user, skipping.");
            return;
        }

        List<Assignments> drafts = List.of(

                // ── COMPLETED ────────────────────────────────────────────────────────

                Assignments.builder()
                        .title("Build a RESTful API with Spring Boot")
                        .description("Design and implement a full CRUD API with JWT authentication and role-based access control.")
                        .subject("Web Development")
                        .startDate(LocalDateTime.now().minusDays(20))
                        .dueDate(LocalDateTime.now().minusDays(7))
                        .status(AssignmentStatus.COMPLETED)
                        .progress(100)
                        .user(user)
                        .build(),

                Assignments.builder()
                        .title("ER Diagram for Student Management System")
                        .description("Create a normalized ER diagram with at least 8 entities and document all relationships.")
                        .subject("Database Systems")
                        .startDate(LocalDateTime.now().minusDays(15))
                        .dueDate(LocalDateTime.now().minusDays(5))
                        .status(AssignmentStatus.COMPLETED)
                        .progress(100)
                        .user(user)
                        .build(),

                // ── OVERDUE ──────────────────────────────────────────────────────────

                Assignments.builder()
                        .title("Essay — Impact of AI on Education")
                        .description("Write a 2000-word academic essay discussing AI tools and their effects on modern learning.")
                        .subject("English Communication")
                        .startDate(LocalDateTime.now().minusDays(14))
                        .dueDate(LocalDateTime.now().minusDays(2))
                        .status(AssignmentStatus.OVERDUE)
                        .progress(40)
                        .user(user)
                        .build(),

                Assignments.builder()
                        .title("Algorithm Analysis Report")
                        .description("Analyze time and space complexity of Merge Sort, Quick Sort, and Heap Sort with benchmarks.")
                        .subject("Data Structures & Algorithms")
                        .startDate(LocalDateTime.now().minusDays(10))
                        .dueDate(LocalDateTime.now().minusDays(1))
                        .status(AssignmentStatus.OVERDUE)
                        .progress(20)
                        .user(user)
                        .build(),

                // ── IN_PROGRESS ──────────────────────────────────────────────────────

                Assignments.builder()
                        .title("UI/UX Prototype — Student Life App")
                        .description("Design a high-fidelity Figma prototype with at least 10 screens and a usability report.")
                        .subject("Human-Computer Interaction")
                        .startDate(LocalDateTime.now().minusDays(5))
                        .dueDate(LocalDateTime.now().plusDays(5))
                        .status(AssignmentStatus.IN_PROGRESS)
                        .progress(60)
                        .user(user)
                        .build(),

                Assignments.builder()
                        .title("Network Topology Lab Report")
                        .description("Configure a simulated network in Cisco Packet Tracer and document the setup with screenshots.")
                        .subject("Computer Networks")
                        .startDate(LocalDateTime.now().minusDays(3))
                        .dueDate(LocalDateTime.now().plusDays(4))
                        .status(AssignmentStatus.IN_PROGRESS)
                        .progress(50)
                        .user(user)
                        .build(),

                Assignments.builder()
                        .title("Mobile App — Expense Tracker")
                        .description("Build a Flutter mobile app with local storage, charts, and category-based expense tracking.")
                        .subject("Mobile Application Development")
                        .startDate(LocalDateTime.now().minusDays(7))
                        .dueDate(LocalDateTime.now().plusDays(7))
                        .status(AssignmentStatus.IN_PROGRESS)
                        .progress(75)
                        .user(user)
                        .build(),

                // ── PENDING ──────────────────────────────────────────────────────────

                Assignments.builder()
                        .title("Research Paper — Cloud Computing Security")
                        .description("Survey current threats, mitigation strategies, and case studies in cloud security.")
                        .subject("Information Security")
                        .startDate(LocalDateTime.now().plusDays(1))
                        .dueDate(LocalDateTime.now().plusDays(14))
                        .status(AssignmentStatus.PENDING)
                        .progress(0)
                        .user(user)
                        .build(),

                Assignments.builder()
                        .title("OOP Design — Library Management System")
                        .description("Implement a full OOP solution in Java using inheritance, polymorphism, and design patterns.")
                        .subject("Object-Oriented Programming")
                        .startDate(LocalDateTime.now().plusDays(2))
                        .dueDate(LocalDateTime.now().plusDays(12))
                        .status(AssignmentStatus.PENDING)
                        .progress(0)
                        .user(user)
                        .build(),

                Assignments.builder()
                        .title("Statistics Project — Survey Data Analysis")
                        .description("Collect survey data from 50+ respondents and perform descriptive and inferential statistical analysis.")
                        .subject("Statistics & Probability")
                        .startDate(LocalDateTime.now().plusDays(3))
                        .dueDate(LocalDateTime.now().plusDays(18))
                        .status(AssignmentStatus.PENDING)
                        .progress(0)
                        .user(user)
                        .build()
        );

        for (Assignments draft : drafts) {

            // Step 1 — save assignment first to get its generated ID
            Assignments saved = assignmentRepository.save(draft);

            // Step 2 — create the owner's schedule row linked to this assignment
            Schedules schedule = scheduleRepository.save(
                    Schedules.builder()
                            .title(saved.getTitle())
                            .description(saved.getDescription())
                            .type(ScheduleType.ONE_TIME)
                            .startTime(saved.getStartDate())
                            .endTime(saved.getDueDate())
                            .assignmentId(saved.getId())   // schedule → assignment
                            .isImportant(false)
                            .user(user)
                            .build()
            );

            // Step 3 — write scheduleId back onto the assignment so the link is bidirectional
            saved.setScheduleId(schedule.getId());        // assignment → schedule
            assignmentRepository.save(saved);

            log.info("Seeded assignment '{}' ↔ scheduleId={}.", saved.getTitle(), schedule.getId());
        }

        log.info("10 assignments + 10 owner schedules seeded for '{}'.", TARGET_EMAIL);
    }
}