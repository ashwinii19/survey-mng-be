////
////
////package com.survey.batch;
////
////import java.util.ArrayList;
////import java.util.List;
////import java.util.stream.Collectors;
////
////import org.springframework.batch.item.Chunk;
////import org.springframework.batch.item.ItemWriter;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.batch.core.configuration.annotation.JobScope;
////import org.springframework.stereotype.Component;
////
////import com.survey.entity.Employee;
////import com.survey.repository.EmployeeRepository;
////import com.survey.repository.EmployeeBatchLogRepository;
////import com.survey.entity.EmployeeBatchLog;
////
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////
////@Component
////@JobScope
////@RequiredArgsConstructor
////@Slf4j
////public class EmployeeItemWriter implements ItemWriter<Employee> {
////
////    private final EmployeeRepository employeeRepository;
////    private final EmployeeBatchLogRepository batchLogRepository;
////
////    @Value("#{jobParameters['batchLogId']}")
////    private Long batchLogId;
////
////    @Override
////    public void write(Chunk<? extends Employee> chunk) throws Exception {
////        List<Employee> employees = new ArrayList<>(chunk.getItems());
////        
////        if (employees.isEmpty()) {
////            return;
////        }
////
////        try {
////            List<String> existingEmails = employeeRepository.findAll().stream()
////                    .map(Employee::getEmail)
////                    .collect(Collectors.toList());
////
////            List<Employee> newEmployees = employees.stream()
////                    .filter(e -> !existingEmails.contains(e.getEmail()))
////                    .collect(Collectors.toList());
////
////            int processed = newEmployees.size();
////            int failed = employees.size() - processed;
////
////            // Save only new employees
////            if (!newEmployees.isEmpty()) {
////                employeeRepository.saveAll(newEmployees);
////                log.info("Saved {} new employees to database", processed);
////            }
////
////            // Update the existing batch log (don't create a new one)
////            if (batchLogId != null) {
////                EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId).orElse(null);
////                if (batchLog != null) {
////                    // Get current counts with null safety
////                    Integer currentProcessed = batchLog.getProcessedCount();
////                    Integer currentFailed = batchLog.getFailedCount();
////                    
////                    
////                    int updatedProcessed = (currentProcessed != null ? currentProcessed : 0) + processed;
////                    int updatedFailed = (currentFailed != null ? currentFailed : 0) + failed;
////                    
////                    batchLog.setProcessedCount(updatedProcessed);
////                    batchLog.setFailedCount(updatedFailed);
////                    
////                    // Update status based on final counts
////                    Integer totalCount = batchLog.getTotalCount();
////                    if (totalCount != null) {
////                        if (updatedFailed == 0 && updatedProcessed > 0) {
////                            batchLog.setStatus("SUCCESS");
////                        } else if (updatedFailed == totalCount) {
////                            batchLog.setStatus("FAILED");
////                        } else if (updatedProcessed > 0 && updatedFailed > 0) {
////                            batchLog.setStatus("PARTIAL_SUCCESS");
////                        } else {
////                            batchLog.setStatus("COMPLETED");
////                        }
////                    }
////                    
////                    batchLogRepository.save(batchLog);
////                    
////                    log.info("Updated batch log {}: +{} processed, +{} failed (Total: P={}, F={}, Status: {})", 
////                            batchLogId, processed, failed, updatedProcessed, updatedFailed, batchLog.getStatus());
////                } else {
////                    log.warn("Batch log not found for ID: {}", batchLogId);
////                }
////            }
////
////        } catch (Exception e) {
////            log.error("Error in EmployeeItemWriter for batchLogId: {}", batchLogId, e);
////            throw e;
////        }
////    }
////}
//
//
//
//
//package com.survey.batch;
//
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.batch.core.configuration.annotation.JobScope;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.survey.entity.Employee;
//import com.survey.entity.EmployeeBatchLog;
//import com.survey.repository.EmployeeRepository;
//import com.survey.repository.EmployeeBatchLogRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@JobScope
//@RequiredArgsConstructor
//@Slf4j
//public class EmployeeItemWriter implements ItemWriter<Employee> {
//
//    private final EmployeeRepository employeeRepository;
//    private final EmployeeBatchLogRepository batchLogRepository;
//    private final ObjectMapper objectMapper;
//
//    @Value("#{jobParameters['batchLogId']}")
//    private Long batchLogId;
//
//    @Override
//    public void write(Chunk<? extends Employee> chunk) throws Exception {
//        List<Employee> employees = new ArrayList<>(chunk.getItems());
//        
//        log.info("🟡 EmployeeItemWriter: Processing {} employees for batch {}", employees.size(), batchLogId);
//        
//        if (employees.isEmpty()) {
//            log.info("📭 No employees to process in this chunk");
//            return;
//        }
//
//        try {
//            // 🟡 SIMPLIFY: Just save all employees (processor already handled validation)
//            List<Employee> savedEmployees = employeeRepository.saveAll(employees);
//            
//            log.info("✅ SUCCESS: Saved {} employees to database for batch {}", savedEmployees.size(), batchLogId);
//            
//            // Extract emails for batch tracking
//            List<String> savedEmails = new ArrayList<>();
//            for (Employee employee : savedEmployees) {
//                savedEmails.add(employee.getEmail());
//                log.debug("📧 Saved employee: {} - {}", employee.getEmployeeId(), employee.getEmail());
//            }
//            
//            // ✅ FIXED: Correct method name and parameters
//            updateBatchLogWithEmails(savedEmails, savedEmployees.size(), 0);
//            
//        } catch (Exception e) {
//            log.error("❌ FAILED: Error saving employees for batch {}: {}", batchLogId, e.getMessage());
//            // Mark all as failed
//            updateBatchLogWithEmails(new ArrayList<>(), 0, employees.size());
//            throw e;
//        }
//    }
//
//    // ✅ FIXED: Correct method name and parameters
//    private void updateBatchLogWithEmails(List<String> processedEmails, int processedCount, int failedCount) {
//        try {
//            if (batchLogId == null) {
//                log.warn("❌ batchLogId is null, cannot update batch log");
//                return;
//            }
//            
//            EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId).orElse(null);
//            if (batchLog == null) {
//                log.warn("❌ Batch log not found for ID: {}", batchLogId);
//                return;
//            }
//
//            // Get current counts with null safety
//            Integer currentProcessed = batchLog.getProcessedCount();
//            Integer currentFailed = batchLog.getFailedCount();
//            
//            // Handle null values by defaulting to 0
//            int updatedProcessed = (currentProcessed != null ? currentProcessed : 0) + processedCount;
//            int updatedFailed = (currentFailed != null ? currentFailed : 0) + failedCount;
//            
//            batchLog.setProcessedCount(updatedProcessed);
//            batchLog.setFailedCount(updatedFailed);
//            
//            // 🟡 CRITICAL: Store batch employee emails for onboarding
//            if (!processedEmails.isEmpty()) {
//                try {
//                    String batchEmailsJson = objectMapper.writeValueAsString(processedEmails);
//                    batchLog.setBatchEmployeeEmails(batchEmailsJson);
//                    log.info("📧 Stored {} emails in batch_employee_emails for onboarding", processedEmails.size());
//                } catch (Exception e) {
//                    log.error("❌ Failed to store batch emails as JSON: {}", e.getMessage());
//                    // Store as comma-separated as fallback
//                    batchLog.setBatchEmployeeEmails(String.join(",", processedEmails));
//                }
//            }
//            
//            // Set completion time
//            batchLog.setCompletedAt(LocalDateTime.now());
//            
//            // ✅ FIXED: Convert Integer to int for comparison
//            // Update status based on counts
//            Integer totalCount = batchLog.getTotalCount();
//            if (totalCount != null && totalCount > 0) {
//                int totalCountInt = totalCount.intValue(); // ✅ Convert to primitive int
//                
//                if (updatedFailed == 0 && updatedProcessed > 0) {
//                    batchLog.setStatus("SUCCESS");
//                } else if (updatedFailed == totalCountInt) {
//                    batchLog.setStatus("FAILED");
//                } else if (updatedProcessed > 0 && updatedFailed > 0) {
//                    batchLog.setStatus("PARTIAL_SUCCESS");
//                } else {
//                    batchLog.setStatus("PROCESSING");
//                }
//            }
//            
//            batchLogRepository.save(batchLog);
//            
//            log.info("📊 Updated batch {}: Processed={}, Failed={}, Status={}", 
//                    batchLogId, updatedProcessed, updatedFailed, batchLog.getStatus());
//            
//        } catch (Exception e) {
//            log.error("❌ Error updating batch log for batchLogId {}: {}", batchLogId, e.getMessage());
//        }
//    }
//}




package com.survey.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.entity.Employee;
import com.survey.entity.EmployeeBatchLog;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.EmployeeBatchLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@JobScope
@RequiredArgsConstructor
@Slf4j
public class EmployeeItemWriter implements ItemWriter<Employee> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeBatchLogRepository batchLogRepository;
    private final ObjectMapper objectMapper;

    @Value("#{jobParameters['batchLogId']}")
    private Long batchLogId;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {
        List<Employee> employees = new ArrayList<>(chunk.getItems());
        
        log.info("🟡 WRITER START: Processing {} employees for batch {}", employees.size(), batchLogId);
        
        if (employees.isEmpty()) {
            log.warn("📭 WRITER: No employees to process in this chunk - chunk size is 0");
            return;
        }

        // 🟡 LOG EACH EMPLOYEE DETAIL BEFORE SAVING
        log.info("📋 EMPLOYEES TO SAVE:");
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            log.info("   {}. {} - {} - {} - Dept: {}", 
                    i + 1, 
                    emp.getEmployeeId(), 
                    emp.getName(), 
                    emp.getEmail(),
                    emp.getDepartment() != null ? 
                        emp.getDepartment().getId() + ":" + emp.getDepartment().getName() : "NULL");
        }

        try {
            log.info("💾 Attempting to save {} employees to database...", employees.size());
            
            List<Employee> savedEmployees = employeeRepository.saveAll(employees);
            
            log.info("✅ WRITER SUCCESS: Saved {} employees to database for batch {}", savedEmployees.size(), batchLogId);
            
            // 🟡 LOG EACH SAVED EMPLOYEE WITH THEIR DATABASE ID
            List<String> savedEmails = new ArrayList<>();
            log.info("📊 SAVED EMPLOYEES:");
            for (Employee employee : savedEmployees) {
                savedEmails.add(employee.getEmail());
                log.info("   💾 ID: {} - {} - {} - {}", 
                        employee.getId(), 
                        employee.getEmployeeId(), 
                        employee.getName(), 
                        employee.getEmail());
            }
            
            updateBatchLogWithEmails(savedEmails, savedEmployees.size(), 0);
            
            log.info("🎉 WRITER COMPLETED: Successfully processed {} employees", savedEmployees.size());
            
        } catch (Exception e) {
            log.error("❌ WRITER FAILED: Error saving {} employees to database: {}", employees.size(), e.getMessage(), e);
            
            // 🟡 LOG THE SPECIFIC ERROR
            log.error("💥 Database Error Details:", e);
            
            updateBatchLogWithEmails(new ArrayList<>(), 0, employees.size());
            throw e;
        }
    }

    private void updateBatchLogWithEmails(List<String> processedEmails, int processedCount, int failedCount) {
        try {
            if (batchLogId == null) {
                log.error("❌ batchLogId is null, cannot update batch log");
                return;
            }
            
            log.info("🔄 Updating batch log {} with {} processed, {} failed emails", batchLogId, processedCount, failedCount);
            
            EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId).orElse(null);
            if (batchLog == null) {
                log.error("❌ Batch log not found for ID: {}", batchLogId);
                return;
            }

            // Get current counts with null safety
            Integer currentProcessed = batchLog.getProcessedCount();
            Integer currentFailed = batchLog.getFailedCount();
            
            // Handle null values by defaulting to 0
            int updatedProcessed = (currentProcessed != null ? currentProcessed : 0) + processedCount;
            int updatedFailed = (currentFailed != null ? currentFailed : 0) + failedCount;
            
            batchLog.setProcessedCount(updatedProcessed);
            batchLog.setFailedCount(updatedFailed);
            
            // 🟡 CRITICAL: Store batch employee emails for onboarding
            if (!processedEmails.isEmpty()) {
                try {
                    String batchEmailsJson = objectMapper.writeValueAsString(processedEmails);
                    batchLog.setBatchEmployeeEmails(batchEmailsJson);
                    log.info("📧 Stored {} emails in batch_employee_emails for onboarding: {}", processedEmails.size(), processedEmails);
                } catch (Exception e) {
                    log.error("❌ Failed to store batch emails as JSON: {}", e.getMessage());
                    // Store as comma-separated as fallback
                    String commaSeparatedEmails = String.join(",", processedEmails);
                    batchLog.setBatchEmployeeEmails(commaSeparatedEmails);
                    log.info("📧 Stored emails as comma-separated: {}", commaSeparatedEmails);
                }
            } else {
                log.warn("⚠️ No emails to store in batch_employee_emails");
            }
            
            // Set completion time if this is the final update
            if (processedCount > 0 || failedCount > 0) {
                batchLog.setCompletedAt(LocalDateTime.now());
            }
            
            // Update status based on counts
            Integer totalCount = batchLog.getTotalCount();
            if (totalCount != null && totalCount > 0) {
                int totalCountInt = totalCount.intValue();
                
                if (updatedFailed == 0 && updatedProcessed > 0) {
                    batchLog.setStatus("SUCCESS");
                    log.info("🏆 BATCH STATUS: SUCCESS - All {} employees processed successfully", updatedProcessed);
                } else if (updatedFailed == totalCountInt) {
                    batchLog.setStatus("FAILED");
                    log.error("💥 BATCH STATUS: FAILED - All {} employees failed", updatedFailed);
                } else if (updatedProcessed > 0 && updatedFailed > 0) {
                    batchLog.setStatus("PARTIAL_SUCCESS");
                    log.info("⚠️ BATCH STATUS: PARTIAL_SUCCESS - {} succeeded, {} failed", updatedProcessed, updatedFailed);
                } else {
                    batchLog.setStatus("PROCESSING");
                    log.info("🔄 BATCH STATUS: PROCESSING - {} processed, {} failed", updatedProcessed, updatedFailed);
                }
            }
            
            batchLogRepository.save(batchLog);
            
            log.info("📈 Updated batch {}: Processed={}, Failed={}, Total={}, Status={}", 
                    batchLogId, updatedProcessed, updatedFailed, 
                    batchLog.getTotalCount(), batchLog.getStatus());
            
        } catch (Exception e) {
            log.error("❌ Error updating batch log for batchLogId {}: {}", batchLogId, e.getMessage(), e);
        }
    }
}