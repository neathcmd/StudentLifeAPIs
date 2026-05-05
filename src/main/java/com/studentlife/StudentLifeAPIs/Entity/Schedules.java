package com.studentlife.StudentLifeAPIs.Entity;

import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType type;

    // ── One-time fields ───────────────────────────────────────────────────────

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // ── Recurring fields ──────────────────────────────────────────────────────

    // Fixed: was @Min(1) @Max(7) which doesn't match JS (0=Sun..6=Sat)
    // Now: 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat
    @Min(0) @Max(6)
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "recurring_start_time")
    private LocalTime recurringStartTime;

    @Column(name = "recurring_end_time")
    private LocalTime recurringEndTime;

    // ── Common fields ─────────────────────────────────────────────────────────

    private String location;

    // Added: lets students mark high-priority events with a star
    @Builder.Default
    @Column(name = "is_important", nullable = false)
    private boolean isImportant = false;

//    /**
//     * NEW FIELD — nullable.
//     * Links this schedule event back to its source assignment.
//     * NULL for regular schedules, non-null for assignment-derived events.
//     *
//     * NOT a @ManyToOne — we keep it as a plain Long to avoid
//     * coupling the Schedule entity to the Assignment entity.
//     */
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}