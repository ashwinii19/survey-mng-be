package com.survey.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_employee_mapping")  // Clear table name
@Getter
@Setter
public class BatchEmployeeMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;  // Clear primary key name
    
    @Column(name = "batch_log_id", nullable = false)
    private Long batchLogId;  // Reference to EmployeeBatchLog
    
    @Column(name = "employee_email", nullable = false)
    private String employeeEmail;  // Employee identifier
    
    @Column(name = "employee_name")
    private String employeeName;   // Optional: for easy reference
    
    @Column(name = "mapping_created_at")
    private LocalDateTime mappingCreatedAt = LocalDateTime.now();  // Clear timestamp name
    
    // Constructor for easy creation
    public BatchEmployeeMapping() {}
    
    public BatchEmployeeMapping(Long batchLogId, String employeeEmail, String employeeName) {
        this.batchLogId = batchLogId;
        this.employeeEmail = employeeEmail;
        this.employeeName = employeeName;
    }
}