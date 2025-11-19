

package com.survey.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import com.survey.entity.Employee;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.EmployeeBatchLogRepository;
import com.survey.entity.EmployeeBatchLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@JobScope
@RequiredArgsConstructor
@Slf4j
public class EmployeeItemWriter implements ItemWriter<Employee> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeBatchLogRepository batchLogRepository;

    @Value("#{jobParameters['batchLogId']}")
    private Long batchLogId;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {
        List<Employee> employees = new ArrayList<>(chunk.getItems());
        
        if (employees.isEmpty()) {
            return;
        }

        try {
            List<String> existingEmails = employeeRepository.findAll().stream()
                    .map(Employee::getEmail)
                    .collect(Collectors.toList());

            List<Employee> newEmployees = employees.stream()
                    .filter(e -> !existingEmails.contains(e.getEmail()))
                    .collect(Collectors.toList());

            int processed = newEmployees.size();
            int failed = employees.size() - processed;

            // Save only new employees
            if (!newEmployees.isEmpty()) {
                employeeRepository.saveAll(newEmployees);
                log.info("Saved {} new employees to database", processed);
            }

            // Update the existing batch log (don't create a new one)
            if (batchLogId != null) {
                EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId).orElse(null);
                if (batchLog != null) {
                    // Get current counts with null safety
                    Integer currentProcessed = batchLog.getProcessedCount();
                    Integer currentFailed = batchLog.getFailedCount();
                    
                    // Handle null values by defaulting to 0
                    int updatedProcessed = (currentProcessed != null ? currentProcessed : 0) + processed;
                    int updatedFailed = (currentFailed != null ? currentFailed : 0) + failed;
                    
                    batchLog.setProcessedCount(updatedProcessed);
                    batchLog.setFailedCount(updatedFailed);
                    
                    // Update status based on final counts
                    Integer totalCount = batchLog.getTotalCount();
                    if (totalCount != null) {
                        if (updatedFailed == 0 && updatedProcessed > 0) {
                            batchLog.setStatus("SUCCESS");
                        } else if (updatedFailed == totalCount) {
                            batchLog.setStatus("FAILED");
                        } else if (updatedProcessed > 0 && updatedFailed > 0) {
                            batchLog.setStatus("PARTIAL_SUCCESS");
                        } else {
                            batchLog.setStatus("COMPLETED");
                        }
                    }
                    
                    batchLogRepository.save(batchLog);
                    
                    log.info("Updated batch log {}: +{} processed, +{} failed (Total: P={}, F={}, Status: {})", 
                            batchLogId, processed, failed, updatedProcessed, updatedFailed, batchLog.getStatus());
                } else {
                    log.warn("Batch log not found for ID: {}", batchLogId);
                }
            }

        } catch (Exception e) {
            log.error("Error in EmployeeItemWriter for batchLogId: {}", batchLogId, e);
            throw e;
        }
    }
}




