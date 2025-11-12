package com.survey.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReminderRequestDTO {

    @NotBlank(message = "Survey name is required")
    private String surveyName; 

    private String departmentName; 

    @NotNull(message = "Scheduled date/time is required")
    private LocalDateTime scheduledAt;

    private Integer intervalInDays; 

    private String message; 
}
