package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.*;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(7)
public class GroupChatSeeder implements CommandLineRunner {

    private final AssignmentRepository        assignmentRepository;
    private final AssignmentMemberRepository  assignmentMemberRepository;
    private final GroupChatMemberRepository   groupChatMemberRepository;
    private final GroupMessageRepository      groupMessageRepository;
    private final UserRepository              userRepository;

    private static final String OWNER_EMAIL = "salmonineath31@gmail.com";

    // ── Conversations per assignment (by index 0–9) ───────────────────────────
    // Each inner list is a sequence of {senderIndex, message}
    // senderIndex: -1 = owner (Neath), 0..N = accepted peer index in that group
    private static final List<List<Object[]>> CONVERSATIONS = List.of(

            // Assignment 0 — "Build a RESTful API" — members: Neath, Dara, Makara (Chenda declined)
            List.of(
                    new Object[]{-1, "Hey everyone! Let's divide the API endpoints. I'll handle auth."},
                    new Object[]{ 0, "Sure! I can take care of the user CRUD endpoints."},
                    new Object[]{ 1, "I'll work on the schedule and assignment endpoints then."},
                    new Object[]{-1, "Perfect. Let's use Postman to document everything as we go."},
                    new Object[]{ 0, "Agreed. Should we use JWT or session-based auth?"},
                    new Object[]{-1, "JWT for sure, it fits our Spring Boot setup better."},
                    new Object[]{ 1, "I'll set up the Swagger docs too so it's easier to test."}
            ),

            // Assignment 1 — "ER Diagram" — members: Neath, Bopha, Piseth
            List.of(
                    new Object[]{-1, "Okay so we need at least 8 entities. I'm thinking Users, Roles, Assignments..."},
                    new Object[]{ 0, "Add Schedules, Notifications, and GroupChat too."},
                    new Object[]{ 1, "Don't forget AssignmentMembers and ReminderLogs."},
                    new Object[]{-1, "That's exactly 8! Great. I'll draw the initial diagram in draw.io."},
                    new Object[]{ 0, "Share the link so we can all edit it at the same time."},
                    new Object[]{-1, "Done, check the group drive. Let me know if the cardinalities look right."},
                    new Object[]{ 1, "The Users to Assignments is one-to-many right? One user can have many assignments."},
                    new Object[]{-1, "Exactly. And Assignments to AssignmentMembers is also one-to-many."}
            ),

            // Assignment 2 — "Essay — AI on Education" — members: Neath, Srey, Rith (Leakhena pending)
            List.of(
                    new Object[]{-1, "This essay is 2000 words. I think we split it into 3 sections."},
                    new Object[]{ 0, "I can write the intro and background on AI in education."},
                    new Object[]{ 1, "I'll cover the impact section — benefits and risks."},
                    new Object[]{-1, "I'll write the conclusion and compile everything. Sound good?"},
                    new Object[]{ 0, "Yes! What citation style are we using?"},
                    new Object[]{-1, "APA. Make sure all your sources are from 2020 onwards."},
                    new Object[]{ 1, "Got it. I found a good IEEE paper on AI tutoring systems."}
            ),

            // Assignment 3 — "Algorithm Analysis Report" — members: Neath, Dara, Bopha (Srey pending)
            List.of(
                    new Object[]{-1, "We need to benchmark Merge Sort, Quick Sort, and Heap Sort."},
                    new Object[]{ 0, "I'll write the Java benchmarks for all three and run them."},
                    new Object[]{ 1, "I can do the theoretical analysis part — Big O for each."},
                    new Object[]{-1, "Nice. I'll write the intro and format the final report."},
                    new Object[]{ 0, "Quick Sort is fastest in practice but worst case is O(n²) right?"},
                    new Object[]{ 1, "Yes, and Heap Sort is O(n log n) guaranteed but has poor cache performance."},
                    new Object[]{-1, "Great points, include that comparison in the analysis section."}
            ),

            // Assignment 4 — "UI/UX Prototype" — members: Neath, Chenda, Rith (Piseth declined)
            List.of(
                    new Object[]{-1, "Let's start with the user flow before jumping into Figma."},
                    new Object[]{ 0, "I mapped out 3 main flows: onboarding, assignment creation, and chat."},
                    new Object[]{ 1, "I'll design the dashboard and assignment list screens."},
                    new Object[]{-1, "I'll handle the chat and notification screens."},
                    new Object[]{ 0, "Should we follow Material Design or create our own design system?"},
                    new Object[]{-1, "Let's take inspiration from Material but keep it custom for the report."},
                    new Object[]{ 1, "Screens are done for dashboard, sharing the Figma link now."}
            ),

            // Assignment 5 — "Network Topology Lab" — members: Neath, Makara, Leakhena
            List.of(
                    new Object[]{-1, "Has everyone installed Cisco Packet Tracer?"},
                    new Object[]{ 0, "Yes! Which topology are we doing — star or hybrid?"},
                    new Object[]{ 1, "The brief says hybrid so we should include both star and bus."},
                    new Object[]{-1, "I'll set up the routers and switches. You two handle the end devices."},
                    new Object[]{ 0, "Done on my end. Ping tests are all successful."},
                    new Object[]{-1, "Great. Let's screenshot every step for the report."}
            ),

            // Assignment 6 — "Mobile App — Expense Tracker" — members: Neath, Bopha, Chenda, Makara (Rith pending)
            List.of(
                    new Object[]{-1, "Flutter project is initialized. I pushed it to GitHub, check your emails."},
                    new Object[]{ 0, "Got it! I'll work on the UI — home screen and add expense form."},
                    new Object[]{ 1, "I'll handle the local database with SQLite and the category model."},
                    new Object[]{ 2, "I can build the charts screen using fl_chart package."},
                    new Object[]{-1, "Perfect split. Let's use feature branches and PR before merging to main."},
                    new Object[]{ 0, "Home screen is done, PR submitted. Can someone review?"},
                    new Object[]{ 1, "Reviewed and approved! Looks clean."},
                    new Object[]{-1, "Merging now. Great work everyone, we're ahead of schedule."}
            ),

            // Assignment 7 — "Research Paper — Cloud Security" — members: Neath, Dara, Leakhena (Piseth pending)
            List.of(
                    new Object[]{-1, "Let's each pick 2 cloud threats to research in depth."},
                    new Object[]{ 0, "I'll take data breaches and insider threats."},
                    new Object[]{ 1, "I'll cover DDoS attacks and misconfiguration risks."},
                    new Object[]{-1, "I'll research insecure APIs and account hijacking then write the intro."},
                    new Object[]{ 0, "Found a great AWS case study on a major data breach from 2023."},
                    new Object[]{-1, "Perfect, include that. Real case studies make the paper much stronger."}
            ),

            // Assignment 8 — "OOP Design — Library System" — members: Neath, Srey, Makara, Rith
            List.of(
                    new Object[]{-1, "We need to use at least 3 design patterns. I'm thinking Factory, Observer, and Strategy."},
                    new Object[]{ 0, "Factory for creating different book types makes sense."},
                    new Object[]{ 1, "Observer for notifications when a book becomes available — that's clean."},
                    new Object[]{ 2, "Strategy pattern for different search algorithms?"},
                    new Object[]{-1, "Exactly. I'll set up the UML class diagram first so we're aligned."},
                    new Object[]{ 0, "Should we use an interface or abstract class for the base Book?"},
                    new Object[]{-1, "Abstract class — we have shared behavior like checkOut() and returnBook()."},
                    new Object[]{ 1, "Diagram looks good, starting on the implementation now."}
            ),

            // Assignment 9 — "Statistics Project" — members: Neath, Bopha, Leakhena
            List.of(
                    new Object[]{-1, "We need 50+ survey responses. Let's share the form on Facebook groups."},
                    new Object[]{ 0, "I'll create the Google Form. What questions should we include?"},
                    new Object[]{ 1, "Focus on study habits and app usage — matches our topic well."},
                    new Object[]{-1, "Good idea. Add questions about hours studied, tools used, and GPA range."},
                    new Object[]{ 0, "Form is live! Got 12 responses already in the first hour."},
                    new Object[]{ 1, "I'll start the descriptive stats once we hit 50."},
                    new Object[]{-1, "We're at 47 now. Almost there!"}
            )
    );

    @Override
    public void run(String... args) {
        log.info("Seeding group chat members and messages...");

        Users owner = userRepository.findByEmail(OWNER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Owner user not found."));

        List<Assignments> assignments = assignmentRepository.findByUser(owner);
        if (assignments.isEmpty()) {
            log.warn("No assignments found. Run AssignmentSeeder first.");
            return;
        }

        if (groupChatMemberRepository.existsByUser(owner)) {
            log.info("Group chat already seeded, skipping.");
            return;
        }

        List<GroupChatMember> chatMembers = new ArrayList<>();
        List<GroupMessage>    messages    = new ArrayList<>();

        for (int i = 0; i < assignments.size(); i++) {
            Assignments assignment = assignments.get(i);
            Long assignmentId      = assignment.getId();

            // Fetch only ACCEPTED members for this assignment
            List<AssignmentMember> accepted = assignmentMemberRepository
                    .findByAssignmentAndStatus(assignment, AssignmentMemberStatus.ACCEPTED);

            List<Users> acceptedUsers = accepted.stream()
                    .map(AssignmentMember::getUser)
                    .toList();

            // ── Seed GroupChatMember for each accepted user ───────────────────
            for (Users u : acceptedUsers) {
                chatMembers.add(GroupChatMember.builder()
                        .assignmentId(assignmentId)
                        .user(u)
                        .build());
            }

            // ── Seed messages ─────────────────────────────────────────────────
            // accepted index 0 = owner, 1..N = peers (in order they were accepted)
            List<Object[]> convo = CONVERSATIONS.get(i);

            // Build a sender lookup: -1 → owner, 0..N → peer by position (skip owner at index 0)
            List<Users> senderList = new ArrayList<>();
            senderList.add(owner); // index -1 maps to this
            for (int k = 1; k < acceptedUsers.size(); k++) {
                senderList.add(acceptedUsers.get(k)); // peers start at slot 1
            }

            for (Object[] line : convo) {
                int    senderIdx = (int) line[0];
                String content   = (String) line[1];

                // senderIdx -1 = owner (senderList[0]), 0 = first peer (senderList[1]), etc.
                int listIdx = senderIdx == -1 ? 0 : senderIdx + 1;

                // Guard: if the peer slot doesn't exist (e.g. peer declined), fall back to owner
                Users sender = listIdx < senderList.size()
                        ? senderList.get(listIdx)
                        : owner;

                messages.add(GroupMessage.builder()
                        .assignmentId(assignmentId)
                        .sender(sender)
                        .content(content)
                        .build());
            }
        }

        groupChatMemberRepository.saveAll(chatMembers);
        groupMessageRepository.saveAll(messages);

        log.info("Seeded {} group chat members and {} messages across {} assignments.",
                chatMembers.size(), messages.size(), assignments.size());
    }
}