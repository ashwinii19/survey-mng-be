package com.survey.dto.response;

import com.survey.entity.EmployeeBatchLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBatchLogResponse {
    private String id;
    private String status;
    private Integer totalCount;
    private Integer processedCount;
    private Integer failedCount;
    private LocalDateTime createdAt;
    
    // Constructor to convert from entity
    public static EmployeeBatchLogResponse fromEntity(EmployeeBatchLog batchLog) {
        return EmployeeBatchLogResponse.builder()
                .id(batchLog.getId().toString())
                .status(batchLog.getStatus())
                .totalCount(batchLog.getTotalCount())
                .processedCount(batchLog.getProcessedCount())
                .failedCount(batchLog.getFailedCount())
                .createdAt(batchLog.getCreatedAt())
                .build();
    }
}