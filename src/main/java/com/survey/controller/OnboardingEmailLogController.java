//package com.survey.controller;
//
//import com.survey.entity.OnboardingEmailLog;
//import com.survey.service.OnboardingEmailService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/onboarding")
//@RequiredArgsConstructor
//
//public class OnboardingEmailLogController {
//
//    private final OnboardingEmailService onboardingEmailService;
//
//    /**
//     * Get all onboarding email logs for the onboarding section
//     */
//    @GetMapping("/email-logs")
//    public ResponseEntity<List<OnboardingEmailLog>> getAllOnboardingEmailLogs() {
//        try {
//            // You'll need to add this method to your repository
//            // List<OnboardingEmailLog> logs = onboardingEmailService.getAllEmailLogs();
//            // return ResponseEntity.ok(logs);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    /**
//     * Get specific onboarding email log details with email lists
//     */
//    @GetMapping("/email-logs/{logId}")
//    public ResponseEntity<Map<String, Object>> getOnboardingEmailLogDetails(@PathVariable Long logId) {
//        try {
//            Map<String, Object> results = onboardingEmailService.getEmailResults(logId);
//            return ResponseEntity.ok(results);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    /**
//     * Trigger onboarding emails for a specific employee batch
//     * This matches your Angular route: /onboarding/onboarded-employees/batch/status/:id
//     */
//    @PostMapping("/batches/{batchId}/send-welcome-emails")
//    public ResponseEntity<Map<String, Object>> triggerOnboardingEmails(@PathVariable Long batchId) {
//        try {
//            onboardingEmailService.sendWelcomeEmailsForBatch(batchId);
//            return ResponseEntity.ok(Map.of(
//                "message", "Onboarding emails triggered successfully",
//                "batchId", batchId,
//                "status", "PROCESSING"
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                "error", "Failed to trigger onboarding emails: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * Check onboarding eligibility for a single employee
//     */
//    @GetMapping("/employees/{email}/eligibility")
//    public ResponseEntity<Map<String, Object>> checkEmployeeEligibility(@PathVariable String email) {
//        try {
//            Boolean eligible = onboardingEmailService.isEmployeeEligibleForOnboarding(email);
//            return ResponseEntity.ok(Map.of(
//                "email", email,
//                "eligible", eligible,
//                "message", eligible ? "Employee is eligible for onboarding" : "Employee is not eligible for onboarding"
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                "error", "Failed to check eligibility: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * Get onboarding statistics for dashboard
//     */
//    @GetMapping("/statistics")
//    public ResponseEntity<Map<String, Object>> getOnboardingStatistics() {
//        try {
//            // You can implement this based on your needs
//            Map<String, Object> stats = Map.of(
//                "totalOnboardingBatches", 0,
//                "emailsSentThisMonth", 0,
//                "successRate", "0%",
//                "pendingBatches", 0
//            );
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//}






package com.survey.controller;

import com.survey.entity.OnboardingEmailLog;
import com.survey.service.OnboardingEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingEmailLogController {

    private final OnboardingEmailService onboardingEmailService;

    /**
     * Get all onboarding email logs - FIXED: Now calls actual service method
     */
    @GetMapping("/email-logs")
    public ResponseEntity<List<OnboardingEmailLog>> getAllOnboardingEmailLogs() {
        try {
            // ✅ FIXED: Call the actual service method
            List<OnboardingEmailLog> logs = onboardingEmailService.getAllEmailLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get specific onboarding email log details with email lists
     */
    @GetMapping("/email-logs/{logId}")
    public ResponseEntity<Map<String, Object>> getOnboardingEmailLogDetails(@PathVariable Long logId) {
        try {
            Map<String, Object> results = onboardingEmailService.getEmailResults(logId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Trigger onboarding emails for a specific employee batch
     */
    @PostMapping("/batches/{batchId}/send-welcome-emails")
    public ResponseEntity<Map<String, Object>> triggerOnboardingEmails(@PathVariable Long batchId) {
        try {
            onboardingEmailService.sendWelcomeEmailsForBatch(batchId);
            return ResponseEntity.ok(Map.of(
                "message", "Onboarding emails triggered successfully",
                "batchId", batchId,
                "status", "PROCESSING"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to trigger onboarding emails: " + e.getMessage()
            ));
        }
    }

    /**
     * Check onboarding eligibility for a single employee
     */
    @GetMapping("/employees/{email}/eligibility")
    public ResponseEntity<Map<String, Object>> checkEmployeeEligibility(@PathVariable String email) {
        try {
            Boolean eligible = onboardingEmailService.isEmployeeEligibleForOnboarding(email);
            return ResponseEntity.ok(Map.of(
                "email", email,
                "eligible", eligible,
                "message", eligible ? "Employee is eligible for onboarding" : "Employee is not eligible for onboarding"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to check eligibility: " + e.getMessage()
            ));
        }
    }

    /**
     * Get onboarding statistics for dashboard - FIXED: Now calls actual service method
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOnboardingStatistics() {
        try {
            // ✅ FIXED: Call the actual service method
            Map<String, Object> stats = onboardingEmailService.getOnboardingStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resend failed emails
     */
    @PostMapping("/email-logs/{logId}/resend-failed")
    public ResponseEntity<Map<String, Object>> resendFailedEmails(@PathVariable Long logId) {
        try {
            onboardingEmailService.resendFailedEmails(logId);
            return ResponseEntity.ok(Map.of(
                "message", "Resending failed emails started",
                "logId", logId,
                "status", "PROCESSING"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to resend emails: " + e.getMessage()
            ));
        }
    }

    /**
     * Get email logs by batch ID
     */
    @GetMapping("/batches/{batchId}/email-logs")
    public ResponseEntity<List<OnboardingEmailLog>> getEmailLogsByBatchId(@PathVariable Long batchId) {
        try {
            List<OnboardingEmailLog> logs = onboardingEmailService.getEmailLogsByBatchId(batchId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}