package com.survey.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReminderRequestDTO {
    private Long surveyId;
    private Long departmentId;
    private String message;
    private LocalDateTime scheduledAt;
    private Integer intervalInDays;
    private boolean active;
}

