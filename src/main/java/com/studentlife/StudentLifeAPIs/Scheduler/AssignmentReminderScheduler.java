package com.studentlife.StudentLifeAPIs.Scheduler;

import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.ReminderLog;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.ReminderLogRepository;
import com.studentlife.StudentLifeAPIs.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Runs every 30 minutes and checks for assignments due in ~72h, ~24h, or ~2h.
 * Sends one reminder email per window per assignment (idempotent via ReminderLog).
 *
 * Each window has a ±15 minute buffer to account for scheduler drift.
 * Example: "72H" window = assignments due between 71h45m and 72h15m from now.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentReminderScheduler {

    private final AssignmentRepository    assignmentRepository;
    private final ReminderLogRepository   reminderLogRepository;
    private final JavaMailSender          mailSender;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    // ── Main job — runs every 30 minutes ──────────────────────────────────────

    @Scheduled(fixedDelay = 30 * 60 * 1000) // every 30 minutes
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[Reminder] Scheduler running at {}", now);

        checkWindow(now, 72, "72H", "72 hours");
        checkWindow(now, 24, "24H", "24 hours");
        checkWindow(now,  2, "2H",  "2 hours");
    }

    // ── Per-window check ──────────────────────────────────────────────────────

    private void checkWindow(LocalDateTime now, int hours, String type, String label) {
        // ±15 minute buffer around the target hour
//        LocalDateTime from = now.plusHours(hours).minusMinutes(15);
//        LocalDateTime to   = now.plusHours(hours).plusMinutes(15);
        LocalDateTime from = now.plusMinutes(1);   // anything due after now
        LocalDateTime to   = now.plusHours(999);   // up to 999 hours away

        List<Assignments> due = assignmentRepository.findDueWithin(from, to, AssignmentStatus.COMPLETED);

        for (Assignments assignment : due) {
            // Skip if already sent this reminder for this assignment
            if (reminderLogRepository.existsByAssignmentIdAndReminderType(assignment.getId(), type)) {
                log.debug("[Reminder] Already sent {} for assignment {}", type, assignment.getId());
                continue;
            }

            try {
                sendReminderEmail(assignment, label);

                // Record that we sent it — prevents duplicate sends
                reminderLogRepository.save(ReminderLog.builder()
                        .assignmentId(assignment.getId())
                        .reminderType(type)
                        .build());

                log.info("[Reminder] Sent {} reminder for assignment '{}' to {}",
                        type, assignment.getTitle(), assignment.getUser().getEmail());

            } catch (Exception e) {
                // Log and continue — don't let one failure block the rest
                log.error("[Reminder] Failed to send {} reminder for assignment {}: {}",
                        type, assignment.getId(), e.getMessage());
            }
        }
    }

    // ── Email builder ─────────────────────────────────────────────────────────

    private void sendReminderEmail(Assignments assignment, String timeLabel) {
        String toEmail    = assignment.getUser().getEmail();
        String fullname       = assignment.getUser().getFullname();
        String title      = assignment.getTitle();
        String subject    = assignment.getSubject();
        String dueFormatted = assignment.getDueDate().format(DISPLAY_FMT);
        String status     = assignment.getStatus().name().replace("_", " ");
        int    progress   = assignment.getProgress();

        // Urgency color — red for 2h, orange for 24h, blue for 72h
        String accentColor = switch (timeLabel) {
            case "2 hours"  -> "#DC2626";
            case "24 hours" -> "#D97706";
            default         -> "#4F46E5";
        };

        String urgencyLabel = switch (timeLabel) {
            case "2 hours"  -> "⚠️ Due Very Soon";
            case "24 hours" -> "⏰ Due Tomorrow";
            default         -> "📅 Upcoming Deadline";
        };

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: %s; margin-bottom: 4px;">StudentLife</h2>
                <p style="color: #6b7280; margin-top: 0;">Assignment Reminder</p>

                <div style="background: %s10; border-left: 4px solid %s; padding: 16px; border-radius: 4px; margin: 20px 0;">
                    <p style="margin: 0; font-weight: bold; color: %s; font-size: 16px;">%s</p>
                    <p style="margin: 4px 0 0; color: #374151;">Due in <strong>%s</strong> — %s</p>
                </div>

                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                        <td style="padding: 8px 0; color: #6b7280; width: 120px;">Assignment</td>
                        <td style="padding: 8px 0; font-weight: bold; color: #111827;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #6b7280;">Subject</td>
                        <td style="padding: 8px 0; color: #111827;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #6b7280;">Due Date</td>
                        <td style="padding: 8px 0; color: #111827;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #6b7280;">Status</td>
                        <td style="padding: 8px 0; color: #111827;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #6b7280;">Progress</td>
                        <td style="padding: 8px 0;">
                            <div style="background: #e5e7eb; border-radius: 9999px; height: 8px; width: 200px;">
                                <div style="background: %s; height: 8px; border-radius: 9999px; width: %d%%;"></div>
                            </div>
                            <span style="font-size: 12px; color: #6b7280; margin-top: 4px; display: block;">%d%% complete</span>
                        </td>
                    </tr>
                </table>

                <p style="color: #9ca3af; font-size: 12px; margin-top: 32px;">
                    You're receiving this because you have an upcoming assignment deadline.<br>
                    — StudentLife
                </p>
            </div>
        """.formatted(
                accentColor, accentColor, accentColor, accentColor,
                urgencyLabel, timeLabel, dueFormatted,
                title, subject, dueFormatted, status,
                accentColor, progress, progress
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[StudentLife] " + urgencyLabel + ": \"" + title + "\" due in " + timeLabel);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reminder email to " + toEmail, e);
        }
    }
}