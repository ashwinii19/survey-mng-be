package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssignmentResponseDTO {
    private Long id;
    private SurveyResponseDTO survey;
    private DepartmentResponseDTO department;
    private EmployeeResponseDTO employee;
    private LocalDateTime assignedAt;
    private LocalDateTime dueDate;
    private boolean reminderSent;
}
