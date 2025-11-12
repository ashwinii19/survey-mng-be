package com.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reminders",
    indexes = {
        @Index(name = "idx_reminder_survey", columnList = "survey_id"),
        @Index(name = "idx_reminder_department", columnList = "department_id"),
        @Index(name = "idx_reminder_scheduled_at", columnList = "scheduledAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String message;

    // Date/time when the reminder is scheduled to be sent
    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    // Number of days between reminders (for recurring reminders)
    @Column(nullable = true)
    private Integer intervalInDays;

    // Whether this reminder is currently active
    @Column(nullable = false)
    private boolean active = true;

    // Whether this reminder has been sent (for the current schedule)
    @Column(nullable = false)
    private boolean sent = false;

    // Timestamp when the reminder was last sent
    @Column(nullable = true)
    private LocalDateTime sentAt;

    // Next automatic scheduled date (updated after each send)
    @Column(nullable = true)
    private LocalDateTime nextScheduledAt;

    // Relationship to Survey
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // Optional relationship to Department (null = all departments)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    // Automatically set next scheduled date if recurring
    @PrePersist
    @PreUpdate
    public void setDefaultNextScheduledAt() {
        if (this.nextScheduledAt == null && this.scheduledAt != null) {
            this.nextScheduledAt = this.scheduledAt;
        }
    }
}
