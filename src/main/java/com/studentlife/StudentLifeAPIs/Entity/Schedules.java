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

    // ── One-time fields ──────────────────────────
    @Column(name = "start_time")
    private LocalDateTime startTime;      // e.g. 2026-03-28T14:00

    @Column(name = "end_time")
    private LocalDateTime endTime;        // e.g. 2026-03-28T16:00

    // ── Recurring fields ─────────────────────────
    @Min(1) @Max(7)
    @Column(name = "day_of_week")
    private Integer dayOfWeek;            // nullable — only used when RECURRING

    @Column(name = "recurring_start_time")
    private LocalTime recurringStartTime; // e.g. 14:00

    @Column(name = "recurring_end_time")
    private LocalTime recurringEndTime;

    private String location;

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
