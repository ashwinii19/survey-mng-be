package com.survey.b.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOM_BATCH_JOB_EXECUTION_PARAMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobExecutionParams {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARAMETER_ID")
    private Long parameterId;       // PRIMARY KEY (AUTO_INCREMENT)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_EXECUTION_ID",
        nullable = false
    )
    private BatchJobExecution jobExecution;   // Foreign Key to Job Execution

    @Column(name = "TYPE_CD", nullable = false)
    private String typeCd;

    @Column(name = "KEY_NAME", nullable = false)
    private String keyName;

    @Column(name = "STRING_VAL")
    private String stringVal;

    @Column(name = "DATE_VAL")
    private LocalDateTime dateVal;

    @Column(name = "LONG_VAL")
    private Long longVal;

    @Column(name = "DOUBLE_VAL")
    private Double doubleVal;

    @Column(name = "IDENTIFYING", nullable = false)
    private String identifying;
}
