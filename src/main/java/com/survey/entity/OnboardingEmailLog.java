//package com.survey.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "onboarding_email_log")
//public class OnboardingEmailLog {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "employee_batch_log_id")
//    private Long employeeBatchLogId;
//
//    private int totalEmployees;
//    private int emailsSent;
//    private int emailsFailed;
//    private String status;
//
//    private LocalDateTime startedAt = LocalDateTime.now();
//    private LocalDateTime completedAt;
//}



package com.survey.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "onboarding_email_log")
public class OnboardingEmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_batch_log_id")
    private Long employeeBatchLogId;

    private int totalEmployees;
    private int emailsSent;
    private int emailsFailed;
    private int ineligibleDueToDate;
    private int dateFormatErrors;
    private String status;

    // 🆕 JSON lists to store actual email addresses
    @Column(columnDefinition = "TEXT")
    private String sentEmailsList;     // JSON: ["email1@company.com", "email2@company.com"]
    
    @Column(columnDefinition = "TEXT")
    private String failedEmailsList;   // JSON: ["email3@company.com"]
    
    @Column(columnDefinition = "TEXT")  
    private String ineligibleEmailsList; // JSON: ["email4@company.com - 2024-11-01", "email5@company.com - 2024-03-05"]
    
    @Column(columnDefinition = "TEXT")
    private String dateErrorEmailsList;  // JSON: ["email6@company.com - invalid_date"]

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime completedAt;
}