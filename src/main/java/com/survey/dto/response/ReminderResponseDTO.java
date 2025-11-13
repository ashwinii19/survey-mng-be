package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReminderResponseDTO {
	private Long id;
	private Long surveyId;
	private String surveyTitle;
	private Long departmentId;
	private String departmentName;
	private String message;
	private LocalDateTime scheduledAt;
	private LocalDateTime nextScheduledAt;
	private Integer intervalInDays;
	private boolean active;
	private boolean sent;
	private LocalDateTime sentAt;
	private String status; 
}
