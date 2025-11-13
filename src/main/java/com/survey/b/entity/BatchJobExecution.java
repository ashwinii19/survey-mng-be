//package com.survey.b.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "CUSTOM_BATCH_JOB_EXECUTION")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BatchJobExecution {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "JOB_EXECUTION_ID")
//    private Long jobExecutionId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "JOB_INSTANCE_ID", nullable = false)
//    private BatchJobInstance jobInstance;
//
//    @Column(name = "START_TIME")
//    private LocalDateTime startTime;
//
//    @Column(name = "END_TIME")
//    private LocalDateTime endTime;
//
//    @Column(name = "STATUS")
//    private String status;
//
//    @Column(name = "EXIT_CODE")
//    private String exitCode;
//
//    @Column(name = "EXIT_MESSAGE", columnDefinition = "TEXT")
//    private String exitMessage;
//
//    @Column(name = "VERSION")
//    private Long version;
//
//    @Column(name = "CREATE_TIME")
//    private LocalDateTime createTime;
//
//    @Column(name = "LAST_UPDATED")
//    private LocalDateTime lastUpdated;
//}




package com.survey.b.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CUSTOM_BATCH_JOB_EXECUTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_INSTANCE_ID", nullable = false)
    private BatchJobInstance jobInstance;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "EXIT_CODE")
    private String exitCode;

    @Column(name = "EXIT_MESSAGE", columnDefinition = "TEXT")
    private String exitMessage;

    @Column(name = "VERSION")
    private Long version;

    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;   

    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;
}
