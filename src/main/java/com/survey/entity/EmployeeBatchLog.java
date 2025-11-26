package com.survey.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "employee_batch_log")
public class EmployeeBatchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalCount;
    private int processedCount;
    private int failedCount;
    private String status;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "batch_employee_emails", columnDefinition = "TEXT")
    private String batchEmployeeEmails;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime completedAt;
}