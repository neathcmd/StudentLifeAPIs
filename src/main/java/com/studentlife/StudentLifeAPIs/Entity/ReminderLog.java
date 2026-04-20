package com.studentlife.StudentLifeAPIs.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "reminder_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "reminder_type"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    /**
     * One of: "72H", "24H", "2H"
     */
    @Column(name = "reminder_type", nullable = false, length = 10)
    private String reminderType;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;
}
