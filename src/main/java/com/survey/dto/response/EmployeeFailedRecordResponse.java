package com.survey.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeFailedRecordResponse {
    private Long id;
    private String employeeData;
    private String errorMessage;
    private String reason;
    private LocalDateTime failedAt;
    private Long batchLogId;
}