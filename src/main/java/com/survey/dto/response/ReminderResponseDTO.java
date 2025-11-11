package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReminderResponseDTO {
    private Long id;
    private String message;
    private LocalDateTime scheduledAt;
    private Integer intervalInDays;
    private boolean active;
    private boolean sent;
    private LocalDateTime sentAt;
    private SurveyResponseDTO survey;
    private DepartmentResponseDTO department;
}
