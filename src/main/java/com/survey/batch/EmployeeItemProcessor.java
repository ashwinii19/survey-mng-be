//
//package com.survey.batch;
//
//import org.modelmapper.ModelMapper;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.core.configuration.annotation.JobScope;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.survey.dto.request.EmployeeRequestDTO;
//import com.survey.entity.Department;
//import com.survey.entity.Employee;
//import com.survey.entity.EmployeeFailedRecord;
//import com.survey.entity.EmployeeBatchLog;
//import com.survey.repository.DepartmentRepository;
//import com.survey.repository.EmployeeFailedRecordRepository;
//import com.survey.repository.EmployeeRepository;
//import com.survey.repository.EmployeeBatchLogRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Optional;
//
//@Component
//@JobScope
//@RequiredArgsConstructor
//@Slf4j
//public class EmployeeItemProcessor implements ItemProcessor<EmployeeRequestDTO, Employee> {
//
//    private final DepartmentRepository departmentRepository;
//    private final EmployeeRepository employeeRepository;
//    private final EmployeeFailedRecordRepository failedRecordRepository;
//    private final EmployeeBatchLogRepository batchLogRepository;
//    private final ModelMapper modelMapper;
//
//    @Value("#{jobParameters['batchLogId']}")
//    private Long batchLogId;
//
//    @Override
//    public Employee process(EmployeeRequestDTO dto) throws Exception {
//        try {
//            log.info("Processing employee: {} with batchLogId: {}", dto.getEmail(), batchLogId);
//
//            // Validate required fields
//            if (dto.getEmployeeId() == null || dto.getEmployeeId().trim().isEmpty()) {
//                saveFailedRecord(dto, "MISSING_EMPLOYEE_ID", "Employee ID is required");
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
//                saveFailedRecord(dto, "MISSING_EMAIL", "Email is required");
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
//                saveFailedRecord(dto, "MISSING_NAME", "Name is required");
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            // Check for duplicate email using custom query method
//            Optional<Employee> existingByEmail = employeeRepository.findByEmail(dto.getEmail().trim().toLowerCase());
//            if (existingByEmail.isPresent()) {
//                saveFailedRecord(dto, "DUPLICATE_EMAIL", "Email already exists: " + dto.getEmail());
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            // Check for duplicate employee ID using custom query method
//            Optional<Employee> existingByEmployeeId = employeeRepository.findByEmployeeId(dto.getEmployeeId().trim());
//            if (existingByEmployeeId.isPresent()) {
//                saveFailedRecord(dto, "DUPLICATE_EMPLOYEE_ID", "Employee ID already exists: " + dto.getEmployeeId());
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            // Validate department exists
//            if (dto.getDepartmentId() == null) {
//                saveFailedRecord(dto, "MISSING_DEPARTMENT", "Department ID is required");
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            Department department = departmentRepository.findById(dto.getDepartmentId())
//                    .orElse(null);
//            if (department == null) {
//                saveFailedRecord(dto, "INVALID_DEPARTMENT", "Department not found with ID: " + dto.getDepartmentId());
//                updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//                return null;
//            }
//
//            // Create employee entity
//            Employee employee = new Employee();
//            employee.setEmployeeId(dto.getEmployeeId().trim());
//            employee.setName(dto.getName().trim());
//            employee.setEmail(dto.getEmail().trim().toLowerCase());
//            employee.setPosition(dto.getPosition() != null ? dto.getPosition().trim() : null);
//            employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
//            employee.setJoinDate(dto.getJoinDate());
//            employee.setDepartment(department);
//
//            // This record will be processed successfully
//            updateBatchLogCounts(1, 0); // 1 processed, 0 failed
//            return employee;
//
//        } catch (Exception e) {
//            log.error("Error processing employee record: {}", dto.getEmail(), e);
//            saveFailedRecord(dto, "PROCESSING_ERROR", e.getMessage());
//            updateBatchLogCounts(0, 1); // 0 processed, 1 failed
//            return null;
//        }
//    }
//
//    private void updateBatchLogCounts(int processedIncrement, int failedIncrement) {
//        try {
//            if (batchLogId != null) {
//                EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId).orElse(null);
//                if (batchLog != null) {
//                    // Get current counts with null safety
//                    Integer currentProcessed = batchLog.getProcessedCount();
//                    Integer currentFailed = batchLog.getFailedCount();
//                    
//                    // Handle null values by defaulting to 0
//                    int updatedProcessed = (currentProcessed != null ? currentProcessed : 0) + processedIncrement;
//                    int updatedFailed = (currentFailed != null ? currentFailed : 0) + failedIncrement;
//                    
//                    batchLog.setProcessedCount(updatedProcessed);
//                    batchLog.setFailedCount(updatedFailed);
//                    
//                    // Update status based on counts
//                    Integer totalCount = batchLog.getTotalCount();
//                    if (totalCount != null && totalCount > 0) {
//                        if (updatedFailed == 0 && updatedProcessed > 0) {
//                            batchLog.setStatus("SUCCESS");
//                        } else if (updatedFailed == totalCount) {
//                            batchLog.setStatus("FAILED");
//                        } else if (updatedProcessed > 0 && updatedFailed > 0) {
//                            batchLog.setStatus("PARTIAL_SUCCESS");
//                        } else {
//                            batchLog.setStatus("PROCESSING");
//                        }
//                    }
//                    
//                    batchLogRepository.save(batchLog);
//                    
//                    log.debug("Updated batch log {}: +{} processed, +{} failed (Total: P={}, F={}, Status: {})", 
//                            batchLogId, processedIncrement, failedIncrement, updatedProcessed, updatedFailed, batchLog.getStatus());
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error updating batch log counts for batchLogId: {}", batchLogId, e);
//        }
//    }
//
//    private void saveFailedRecord(EmployeeRequestDTO dto, String reason, String errorMessage) {
//        try {
//            // Only save failed record if batchLogId is available
//            if (batchLogId != null) {
//                EmployeeFailedRecord failedRecord = new EmployeeFailedRecord();
//                failedRecord.setEmployeeData(convertToJsonString(dto));
//                failedRecord.setReason(reason);
//                failedRecord.setErrorMessage(errorMessage != null ? errorMessage : "Unknown error");
//                failedRecord.setBatchLogId(batchLogId);
//                failedRecordRepository.save(failedRecord);
//                log.warn("Saved failed record for employee: {}, reason: {}", dto.getEmail(), reason);
//            } else {
//                log.warn("batchLogId is null, skipping failed record save for: {}", dto.getEmail());
//            }
//        } catch (Exception e) {
//            log.error("Failed to save failed record for employee: {}", dto.getEmail(), e);
//        }
//    }
//
//    private String convertToJsonString(EmployeeRequestDTO dto) {
//        try {
//            // Handle null values safely
//            return String.format(
//                "{\"employeeId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"position\":\"%s\",\"status\":\"%s\",\"joinDate\":\"%s\",\"departmentId\":%s}",
//                dto.getEmployeeId() != null ? dto.getEmployeeId().replace("\"", "\\\"") : "",
//                dto.getName() != null ? dto.getName().replace("\"", "\\\"") : "",
//                dto.getEmail() != null ? dto.getEmail().replace("\"", "\\\"") : "",
//                dto.getPosition() != null ? dto.getPosition().replace("\"", "\\\"") : "",
//                dto.getStatus() != null ? dto.getStatus() : "ACTIVE",
//                dto.getJoinDate() != null ? dto.getJoinDate().toString() : "",
//                dto.getDepartmentId() != null ? dto.getDepartmentId() : "null"
//            );
//        } catch (Exception e) {
//            log.error("Error converting employee data to JSON: {}", dto.getEmail(), e);
//            return "{\"error\":\"Failed to convert employee data\"}";
//        }
//    }
//}




package com.survey.batch;

import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.EmployeeFailedRecord;
import com.survey.entity.EmployeeBatchLog;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeFailedRecordRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.EmployeeBatchLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Component
@JobScope
@RequiredArgsConstructor
@Slf4j
public class EmployeeItemProcessor implements ItemProcessor<EmployeeRequestDTO, Employee> {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeFailedRecordRepository failedRecordRepository;
    private final EmployeeBatchLogRepository batchLogRepository;
    private final ModelMapper modelMapper;

    @Value("#{jobParameters['batchLogId']}")
    private Long batchLogId;

    @Override
    public Employee process(EmployeeRequestDTO dto) throws Exception {
        log.info("🟡 PROCESSOR START: Processing employee - ID: '{}', Name: '{}', Email: '{}'", 
                dto.getEmployeeId(), dto.getName(), dto.getEmail());
        
        // 🟡 LOG ALL RAW DATA
        log.info("📋 RAW DTO DATA - EmployeeId: '{}', Name: '{}', Email: '{}', Position: '{}', Status: '{}', JoinDate: '{}', DepartmentId: {}", 
                dto.getEmployeeId(), 
                dto.getName(), 
                dto.getEmail(), 
                dto.getPosition(), 
                dto.getStatus(), 
                dto.getJoinDate(), 
                dto.getDepartmentId());

        try {
            // Validate required fields
            if (dto.getEmployeeId() == null || dto.getEmployeeId().trim().isEmpty()) {
                log.warn("❌ VALIDATION FAILED: MISSING_EMPLOYEE_ID for email: {}", dto.getEmail());
                saveFailedRecord(dto, "MISSING_EMPLOYEE_ID", "Employee ID is required");
                updateBatchLogCounts(0, 1);
                return null;
            }

            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                log.warn("❌ VALIDATION FAILED: MISSING_EMAIL for employeeId: {}", dto.getEmployeeId());
                saveFailedRecord(dto, "MISSING_EMAIL", "Email is required");
                updateBatchLogCounts(0, 1);
                return null;
            }

            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                log.warn("❌ VALIDATION FAILED: MISSING_NAME for {} - {}", dto.getEmployeeId(), dto.getEmail());
                saveFailedRecord(dto, "MISSING_NAME", "Name is required");
                updateBatchLogCounts(0, 1);
                return null;
            }

            // Trim and clean data
            String cleanedEmployeeId = dto.getEmployeeId().trim();
            String cleanedEmail = dto.getEmail().trim().toLowerCase();
            String cleanedName = dto.getName().trim();

            log.info("🧹 CLEANED DATA - EmployeeId: '{}', Email: '{}', Name: '{}'", 
                    cleanedEmployeeId, cleanedEmail, cleanedName);

            // Check for duplicate email
            Optional<Employee> existingByEmail = employeeRepository.findByEmail(cleanedEmail);
            if (existingByEmail.isPresent()) {
                log.warn("❌ VALIDATION FAILED: DUPLICATE_EMAIL - '{}' already exists in database", cleanedEmail);
                saveFailedRecord(dto, "DUPLICATE_EMAIL", "Email already exists: " + cleanedEmail);
                updateBatchLogCounts(0, 1);
                return null;
            } else {
                log.info("✅ EMAIL CHECK: '{}' is available", cleanedEmail);
            }

            // Check for duplicate employee ID
            Optional<Employee> existingByEmployeeId = employeeRepository.findByEmployeeId(cleanedEmployeeId);
            if (existingByEmployeeId.isPresent()) {
                log.warn("❌ VALIDATION FAILED: DUPLICATE_EMPLOYEE_ID - '{}' already exists in database", cleanedEmployeeId);
                saveFailedRecord(dto, "DUPLICATE_EMPLOYEE_ID", "Employee ID already exists: " + cleanedEmployeeId);
                updateBatchLogCounts(0, 1);
                return null;
            } else {
                log.info("✅ EMPLOYEE_ID CHECK: '{}' is available", cleanedEmployeeId);
            }

            // Validate department exists
            if (dto.getDepartmentId() == null) {
                log.warn("❌ VALIDATION FAILED: MISSING_DEPARTMENT for {} - {}", cleanedEmployeeId, cleanedEmail);
                saveFailedRecord(dto, "MISSING_DEPARTMENT", "Department ID is required");
                updateBatchLogCounts(0, 1);
                return null;
            }

            log.info("🔍 Checking department with ID: {}", dto.getDepartmentId());
            Department department = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
            if (department == null) {
                log.warn("❌ VALIDATION FAILED: INVALID_DEPARTMENT - Department ID {} not found in database", dto.getDepartmentId());
                saveFailedRecord(dto, "INVALID_DEPARTMENT", "Department not found with ID: " + dto.getDepartmentId());
                updateBatchLogCounts(0, 1);
                return null;
            }

            log.info("✅ DEPARTMENT FOUND: ID {} - Name: {}", department.getId(), department.getName());

            // Create employee entity
            Employee employee = new Employee();
            employee.setEmployeeId(cleanedEmployeeId);
            employee.setName(cleanedName);
            employee.setEmail(cleanedEmail);
            employee.setPosition(dto.getPosition() != null ? dto.getPosition().trim() : "Not Specified");
            employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
            employee.setJoinDate(dto.getJoinDate());
            employee.setDepartment(department);

            log.info("✅ PROCESSOR SUCCESS: Created employee entity - ID: {}, Email: {}, Name: {}, Department: {}", 
                    employee.getEmployeeId(), employee.getEmail(), employee.getName(), 
                    employee.getDepartment() != null ? employee.getDepartment().getName() : "NULL");

            updateBatchLogCounts(1, 0);
            return employee;

        } catch (Exception e) {
            log.error("❌ PROCESSOR ERROR for {}: {}", dto.getEmail(), e.getMessage(), e);
            saveFailedRecord(dto, "PROCESSING_ERROR", e.getMessage());
            updateBatchLogCounts(0, 1);
            return null;
        }
    }

    private void updateBatchLogCounts(int processedIncrement, int failedIncrement) {
        try {
            if (batchLogId == null) {
                log.warn("⚠️ batchLogId is null, cannot update batch log counts");
                return;
            }
            
            EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId).orElse(null);
            if (batchLog == null) {
                log.warn("⚠️ Batch log not found for ID: {}", batchLogId);
                return;
            }

            // Get current counts with null safety
            Integer currentProcessed = batchLog.getProcessedCount();
            Integer currentFailed = batchLog.getFailedCount();
            
            // Handle null values by defaulting to 0
            int updatedProcessed = (currentProcessed != null ? currentProcessed : 0) + processedIncrement;
            int updatedFailed = (currentFailed != null ? currentFailed : 0) + failedIncrement;
            
            batchLog.setProcessedCount(updatedProcessed);
            batchLog.setFailedCount(updatedFailed);
            
            // Update status based on counts
            Integer totalCount = batchLog.getTotalCount();
            if (totalCount != null && totalCount > 0) {
                if (updatedFailed == 0 && updatedProcessed > 0) {
                    batchLog.setStatus("SUCCESS");
                } else if (updatedFailed == totalCount) {
                    batchLog.setStatus("FAILED");
                } else if (updatedProcessed > 0 && updatedFailed > 0) {
                    batchLog.setStatus("PARTIAL_SUCCESS");
                } else {
                    batchLog.setStatus("PROCESSING");
                }
            }
            
            batchLogRepository.save(batchLog);
            
            log.debug("📊 Updated batch log {}: +{} processed, +{} failed (Total: P={}, F={}, Status: {})", 
                    batchLogId, processedIncrement, failedIncrement, updatedProcessed, updatedFailed, batchLog.getStatus());
                    
        } catch (Exception e) {
            log.error("❌ Error updating batch log counts for batchLogId: {}", batchLogId, e);
        }
    }

    private void saveFailedRecord(EmployeeRequestDTO dto, String reason, String errorMessage) {
        try {
            if (batchLogId == null) {
                log.warn("⚠️ batchLogId is null, skipping failed record save for: {}", dto.getEmail());
                return;
            }
            
            EmployeeFailedRecord failedRecord = new EmployeeFailedRecord();
            failedRecord.setEmployeeData(convertToJsonString(dto));
            failedRecord.setReason(reason);
            failedRecord.setErrorMessage(errorMessage != null ? errorMessage : "Unknown error");
            failedRecord.setBatchLogId(batchLogId);
            failedRecordRepository.save(failedRecord);
            
            log.warn("📝 Saved failed record for employee: {}, reason: {}, error: {}", dto.getEmail(), reason, errorMessage);
            
        } catch (Exception e) {
            log.error("❌ Failed to save failed record for employee: {}", dto.getEmail(), e);
        }
    }

    private String convertToJsonString(EmployeeRequestDTO dto) {
        try {
            return String.format(
                "{\"employeeId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"position\":\"%s\",\"status\":\"%s\",\"joinDate\":\"%s\",\"departmentId\":%s}",
                dto.getEmployeeId() != null ? dto.getEmployeeId().replace("\"", "\\\"") : "",
                dto.getName() != null ? dto.getName().replace("\"", "\\\"") : "",
                dto.getEmail() != null ? dto.getEmail().replace("\"", "\\\"") : "",
                dto.getPosition() != null ? dto.getPosition().replace("\"", "\\\"") : "",
                dto.getStatus() != null ? dto.getStatus() : "ACTIVE",
                dto.getJoinDate() != null ? dto.getJoinDate().toString() : "",
                dto.getDepartmentId() != null ? dto.getDepartmentId() : "null"
            );
        } catch (Exception e) {
            log.error("❌ Error converting employee data to JSON: {}", dto.getEmail(), e);
            return "{\"error\":\"Failed to convert employee data\"}";
        }
    }
}