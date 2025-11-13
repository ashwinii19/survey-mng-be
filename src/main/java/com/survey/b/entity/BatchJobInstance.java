package com.survey.b.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CUSTOM_BATCH_JOB_INSTANCE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_INSTANCE_ID")
    private Long jobInstanceId;

    @Column(name = "JOB_NAME", nullable = false)
    private String jobName;

    @Column(name = "JOB_KEY", nullable = false)
    private String jobKey;

    @Column(name = "VERSION")
    private Long version;

    // Remove cascade to avoid issues
    @OneToMany(mappedBy = "jobInstance", fetch = FetchType.LAZY)
    private Set<BatchJobExecution> jobExecutions = new HashSet<>();
}