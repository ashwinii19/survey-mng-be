package com.survey.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AssignmentRequestDTO {

    @NotNull(message = "Survey ID is required")
    private Long surveyId;

    @Size(min = 1, message = "At least one department or employee must be assigned")
    private List<Long> departmentIds;

    private List<Long> employeeIds;

    @FutureOrPresent(message = "Due date must be in the future or present")
    private LocalDateTime dueDate;
}
