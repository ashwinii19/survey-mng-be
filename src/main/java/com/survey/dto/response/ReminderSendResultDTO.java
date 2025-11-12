package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReminderSendResultDTO {
    private Long reminderId;
    private String surveyTitle;
    private String departmentName;
    private int totalEmployees;
    private int pendingCount;
    private List<String> sentTo;
    private List<String> failed;
    private LocalDateTime timestamp;
}
