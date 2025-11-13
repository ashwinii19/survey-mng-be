//package com.survey.b.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "CUSTOM_BATCH_STEP_EXECUTION")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BatchStepExecution {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "STEP_EXECUTION_ID")
//    private Long stepExecutionId;
//
//    @ManyToOne
//    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false)
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private BatchJobExecution jobExecution;
//
//    @Column(name = "VERSION")
//    private Long version;
//
//    @Column(name = "STEP_NAME", nullable = false)
//    private String stepName;
//
//    @Column(name = "START_TIME", nullable = false)
//    private LocalDateTime startTime;
//
//    @Column(name = "END_TIME")
//    private LocalDateTime endTime;
//
//    @Column(name = "STATUS")
//    private String status;
//
//    @Column(name = "COMMIT_COUNT")
//    private Long commitCount;
//
//    @Column(name = "READ_COUNT")
//    private Long readCount;
//
//    @Column(name = "FILTER_COUNT")
//    private Long filterCount;
//
//    @Column(name = "WRITE_COUNT")
//    private Long writeCount;
//
//    @Column(name = "EXIT_CODE")
//    private String exitCode;
//
//    @Column(name = "EXIT_MESSAGE", length = 2500)
//    private String exitMessage;
//
//    @Column(name = "LAST_UPDATED")
//    private LocalDateTime lastUpdated;
//}