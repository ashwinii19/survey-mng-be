package com.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String message;
	private LocalDateTime scheduledAt;
	private Integer intervalInDays;

    private boolean active;
	private boolean sent;
	private LocalDateTime sentAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	private Survey survey;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;
}
