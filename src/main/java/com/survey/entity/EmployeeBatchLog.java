package com.survey.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class EmployeeBatchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalCount;
    private int processedCount;
    private int failedCount;
    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();
}
