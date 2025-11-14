package com.survey.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_failed_records")
@Getter
@Setter
public class EmployeeFailedRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String employeeData; // Store the raw CSV line or employee details

    private String errorMessage;
    private String reason;
    
    private LocalDateTime failedAt = LocalDateTime.now();
    
    // Optional: Link to batch log
    private Long batchLogId;
}