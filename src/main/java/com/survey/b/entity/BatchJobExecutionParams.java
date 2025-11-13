//package com.survey.b.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "CUSTOM_BATCH_JOB_EXECUTION_PARAMS")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BatchJobExecutionParams {
//
//    @Id
//    @Column(name = "JOB_EXECUTION_ID")
//    private Long jobExecutionId;
//
//    // Remove the paramId field since it doesn't exist in your table
//    // @GeneratedValue(strategy = GenerationType.IDENTITY)
//    // @Column(name = "PARAM_ID")
//    // private Long paramId;
//
//    @ManyToOne
//    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = false, updatable = false)
//    private BatchJobExecution jobExecution;
//
//    @Column(name = "TYPE_CD", nullable = false)
//    private String typeCd;
//
//    @Column(name = "KEY_NAME", nullable = false)
//    private String keyName;
//
//    @Column(name = "STRING_VAL")
//    private String stringVal;
//
//    @Column(name = "DATE_VAL")
//    private LocalDateTime dateVal;
//
//    @Column(name = "LONG_VAL")
//    private Long longVal;
//
//    @Column(name = "DOUBLE_VAL")
//    private Double doubleVal;
//
//    @Column(name = "IDENTIFYING", nullable = false)
//    private String identifying;
//}