package com.survey.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReminderRequestDTO {

    @NotNull(message = "Survey ID is required")
    private Long surveyId;

    private Long departmentId;

    @NotBlank(message = "Reminder message is required")
    private String message;

    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;

    @Min(value = 1, message = "Interval must be at least 1 day")
    private Integer intervalInDays;

    private boolean active;
}
