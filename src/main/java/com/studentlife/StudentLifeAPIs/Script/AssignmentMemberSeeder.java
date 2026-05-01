package com.studentlife.StudentLifeAPIs.Script;

import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(6)
public class AssignmentMemberSeeder implements CommandLineRunner {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMemberRepository assignmentMemberRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    private static final String OWNER_EMAIL = "salmonineath31@gmail.com";

    // Each assignment gets a different subset — indices into the peer list below
    // 10 assignments, each with 2–4 peers (no two groups are the same)
    private static final int[][] PEER_SETS = {
            {0, 2, 5},       // assignment 0 — Dara, Chenda, Makara
            {1, 3},          // assignment 1 — Bopha, Piseth
            {4, 6, 7},       // assignment 2 — Srey, Leakhena, Rith
            {0, 1, 4},       // assignment 3 — Dara, Bopha, Srey
            {2, 3, 7},       // assignment 4 — Chenda, Piseth, Rith
            {5, 6},          // assignment 5 — Makara, Leakhena
            {1, 2, 5, 7},    // assignment 6 — Bopha, Chenda, Makara, Rith
            {0, 3, 6},       // assignment 7 — Dara, Piseth, Leakhena
            {4, 5, 7},       // assignment 8 — Srey, Makara, Rith
            {1, 6},          // assignment 9 — Bopha, Leakhena
    };

    // ACCEPTED/INVITED/DECLINED per peer slot — adds realism
    // true = ACCEPTED, false = INVITED (pending), null = DECLINED
    private static final Boolean[][] PEER_STATUSES = {
            {true,  null, true},         // assignment 0
            {true,  true},               // assignment 1
            {true,  false, true},        // assignment 2
            {true,  true,  false},       // assignment 3
            {true,  null,  true},        // assignment 4
            {true,  true},               // assignment 5
            {true,  true,  true, false}, // assignment 6
            {true,  false, true},        // assignment 7
            {true,  true,  true},        // assignment 8
            {true,  true},               // assignment 9
    };

    private static final List<String> PEER_EMAILS = List.of(
            "dara.sopheak@gmail.com",
            "vanna.bopha@gmail.com",
            "chenda.ratana@gmail.com",
            "piseth.kosal@gmail.com",
            "sreyleak.mony@gmail.com",
            "makara.vibol@gmail.com",
            "leakhena.soda@gmail.com",
            "bunthoeun.rith@gmail.com"
    );

    @Override
    public void run(String... args) {
        log.info("Seeding assignment members...");

        Users owner = userRepository.findByEmail(OWNER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Owner user not found."));

        List<Assignments> assignments = assignmentRepository.findByUser(owner);
        if (assignments.isEmpty()) {
            log.warn("No assignments found for owner. Run AssignmentSeeder first.");
            return;
        }

        if (assignmentMemberRepository.existsByUser(owner)) {
            log.info("Assignment members already seeded, skipping.");
            return;
        }

        List<Users> peers = new ArrayList<>();
        for (String email : PEER_EMAILS) {
            userRepository.findByEmail(email).ifPresent(peers::add);
        }

        List<AssignmentMember> membersToSave   = new ArrayList<>();
        List<Schedules>        schedulesToSave  = new ArrayList<>();

        for (int i = 0; i < assignments.size(); i++) {
            Assignments assignment = assignments.get(i);

            // ── Owner is always ACCEPTED ──────────────────────────────────────
            membersToSave.add(AssignmentMember.builder()
                    .assignment(assignment)
                    .user(owner)
                    .status(AssignmentMemberStatus.ACCEPTED)
                    .build());

            // ── Peers ─────────────────────────────────────────────────────────
            int[]     peerIndices  = PEER_SETS[i];
            Boolean[] peerStatuses = PEER_STATUSES[i];

            for (int j = 0; j < peerIndices.length; j++) {
                Users peer   = peers.get(peerIndices[j]);
                Boolean flag = peerStatuses[j];

                AssignmentMemberStatus status = flag == null
                        ? AssignmentMemberStatus.DECLINED
                        : flag
                          ? AssignmentMemberStatus.ACCEPTED
                          : AssignmentMemberStatus.INVITED;

                membersToSave.add(AssignmentMember.builder()
                        .assignment(assignment)
                        .user(peer)
                        .status(status)
                        // Give INVITED members a token expiry 48h from now
                        .tokenExpiresAt(status == AssignmentMemberStatus.INVITED
                                ? Instant.now().plusSeconds(48 * 3600)
                                : null)
                        .build());

                // ── Seed a schedule for ACCEPTED peers ───────────────────────
                if (status == AssignmentMemberStatus.ACCEPTED) {
                    schedulesToSave.add(Schedules.builder()
                            .title(assignment.getTitle())
                            .description("Collaborative assignment: " + assignment.getDescription())
                            .type(ScheduleType.ONE_TIME)
                            .startTime(assignment.getStartDate())
                            .endTime(assignment.getDueDate())
                            .assignmentId(assignment.getId())
                            .isImportant(false)
                            .user(peer)
                            .build());
                }
            }
        }

        assignmentMemberRepository.saveAll(membersToSave);
        scheduleRepository.saveAll(schedulesToSave);

        log.info("Seeded {} assignment members and {} peer schedules.",
                membersToSave.size(), schedulesToSave.size());
    }
}