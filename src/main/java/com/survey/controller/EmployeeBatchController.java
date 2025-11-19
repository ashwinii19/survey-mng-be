//
//
//
//package com.survey.controller;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.survey.entity.EmployeeBatchLog;
//import com.survey.repository.EmployeeBatchLogRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@RestController
//@RequestMapping("/api/employees/batch")
//@RequiredArgsConstructor
//@Slf4j
//public class EmployeeBatchController {
//
//    private final JobLauncher jobLauncher;
//    private final Job importEmployeeJob;
//    private final EmployeeBatchLogRepository batchLogRepository;
//
//    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, String>> importEmployees(
//            @RequestParam("file") MultipartFile file) {
//
//        // Validate file
//        if (file == null || file.isEmpty()) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "File is required"));
//        }
//
//        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Only CSV files are supported"));
//        }
//
//        // Count total records in CSV first
//        int totalRecords = countRecordsInCSV(file);
//        
//        // Create batch log with total count
//        EmployeeBatchLog batchLog = new EmployeeBatchLog();
//        batchLog.setStatus("STARTED");
//        batchLog.setCreatedAt(LocalDateTime.now());
//        batchLog.setTotalCount(totalRecords);
//        batchLog.setProcessedCount(0);
//        batchLog.setFailedCount(0);
//        batchLog = batchLogRepository.save(batchLog);
//
//        log.info("Created batch log with ID: {} for file: {} with {} total records", 
//                batchLog.getId(), file.getOriginalFilename(), totalRecords);
//
//        Path tempFilePath = null;
//        try {
//            // Save uploaded CSV to a temporary file
//            tempFilePath = Files.createTempFile("employee_import_", ".csv");
//            File tempFile = tempFilePath.toFile();
//
//            try (InputStream inputStream = file.getInputStream();
//                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
//
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, bytesRead);
//                }
//            }
//
//            log.info("Saved uploaded file to temporary location: {}", tempFilePath);
//
//            // Build job parameters (file path + timestamp + batchLogId)
//            var jobParams = new JobParametersBuilder()
//                    .addString("filePath", tempFilePath.toAbsolutePath().toString())
//                    .addLong("time", System.currentTimeMillis()) // ensures a unique job instance
//                    .addLong("batchLogId", batchLog.getId()) // for tracking failed records
//                    .toJobParameters();
//
//            // Update batch log status to processing
//            batchLog.setStatus("PROCESSING");
//            batchLogRepository.save(batchLog);
//
//            // Launch the batch job
//            log.info("Launching batch job for batchLogId: {}", batchLog.getId());
//            jobLauncher.run(importEmployeeJob, jobParams);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Employee batch import job started successfully.",
//                "batchLogId", batchLog.getId().toString(),
//                "totalRecords", String.valueOf(totalRecords)
//            ));
//
//        } catch (Exception e) {
//            // Update batch log status to failed if job launch fails
//            batchLog.setStatus("FAILED");
//            batchLogRepository.save(batchLog);
//            
//            log.error("Batch import failed for batchLogId: {}", batchLog.getId(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of(
//                        "message", "Employee batch import failed: " + e.getMessage(),
//                        "batchLogId", batchLog.getId().toString()
//                    ));
//        } finally {
//            // Clean up temp file
//            if (tempFilePath != null) {
//                try {
//                    Files.delete(tempFilePath);
//                    log.info("Cleaned up temporary file: {}", tempFilePath);
//                } catch (Exception e) {
//                    log.warn("Could not delete temporary file: {}", tempFilePath, e);
//                }
//            }
//        }
//    }
//
//    private int countRecordsInCSV(MultipartFile file) {
//        try {
//            String content = new String(file.getBytes());
//            String[] lines = content.split("\r\n|\r|\n");
//            // Subtract 1 for header row, ensure at least 0
//            int recordCount = Math.max(0, lines.length - 1);
//            log.info("Counted {} records in CSV file ({} total lines)", recordCount, lines.length);
//            return recordCount;
//        } catch (Exception e) {
//            log.warn("Could not count records in CSV file, using default count", e);
//            return 0; // Return 0 if cannot count
//        }
//    }
//    
//    
//    @GetMapping("/status/{batchLogId}")
//    public ResponseEntity<?> getBatchStatus(@PathVariable Long batchLogId) {
//        try {
//            EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId)
//                    .orElseThrow(() -> new RuntimeException("Batch log not found with ID: " + batchLogId));
//            
//            return ResponseEntity.ok(batchLog);
//        } catch (Exception e) {
//            log.error("Error fetching batch status for ID: {}", batchLogId, e);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", "Batch log not found with ID: " + batchLogId));
//        }
//    }
//}



package com.survey.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.survey.entity.EmployeeBatchLog;
import com.survey.repository.EmployeeBatchLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/employees/batch")
@RequiredArgsConstructor
@Slf4j
public class EmployeeBatchController {

    private final JobLauncher jobLauncher;
    private final Job importEmployeeJob;
    private final EmployeeBatchLogRepository batchLogRepository;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> importEmployees(
            @RequestParam("file") MultipartFile file) {

        // Validate file
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File is required"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only CSV files are supported"));
        }

        // Count total records in CSV first
        int totalRecords = countRecordsInCSV(file);
        
        // Create batch log with total count
        EmployeeBatchLog batchLog = new EmployeeBatchLog();
        batchLog.setStatus("STARTED");
        batchLog.setCreatedAt(LocalDateTime.now());
        batchLog.setTotalCount(totalRecords);
        batchLog.setProcessedCount(0);
        batchLog.setFailedCount(0);
        batchLog = batchLogRepository.save(batchLog);

        log.info("Created batch log with ID: {} for file: {} with {} total records", 
                batchLog.getId(), file.getOriginalFilename(), totalRecords);

        Path tempFilePath = null;
        try {
            // Save uploaded CSV to a temporary file
            tempFilePath = Files.createTempFile("employee_import_", ".csv");
            File tempFile = tempFilePath.toFile();

            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            log.info("Saved uploaded file to temporary location: {}", tempFilePath);

            // Build job parameters (file path + timestamp + batchLogId)
            var jobParams = new JobParametersBuilder()
                    .addString("filePath", tempFilePath.toAbsolutePath().toString())
                    .addLong("time", System.currentTimeMillis()) // ensures a unique job instance
                    .addLong("batchLogId", batchLog.getId()) // for tracking failed records
                    .toJobParameters();

            // Update batch log status to processing
            batchLog.setStatus("PROCESSING");
            batchLogRepository.save(batchLog);

            // Launch the batch job
            log.info("Launching batch job for batchLogId: {}", batchLog.getId());
            jobLauncher.run(importEmployeeJob, jobParams);

            return ResponseEntity.ok(Map.of(
                "message", "Employee batch import job started successfully.",
                "batchLogId", batchLog.getId().toString(),
                "totalRecords", String.valueOf(totalRecords)
            ));

        } catch (Exception e) {
            // Update batch log status to failed if job launch fails
            batchLog.setStatus("FAILED");
            batchLogRepository.save(batchLog);
            
            log.error("Batch import failed for batchLogId: {}", batchLog.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "message", "Employee batch import failed: " + e.getMessage(),
                        "batchLogId", batchLog.getId().toString()
                    ));
        } finally {
            // Clean up temp file
            if (tempFilePath != null) {
                try {
                    Files.delete(tempFilePath);
                    log.info("Cleaned up temporary file: {}", tempFilePath);
                } catch (Exception e) {
                    log.warn("Could not delete temporary file: {}", tempFilePath, e);
                }
            }
        }
    }

    private int countRecordsInCSV(MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            String[] lines = content.split("\r\n|\r|\n");
            // Subtract 1 for header row, ensure at least 0
            int recordCount = Math.max(0, lines.length - 1);
            log.info("Counted {} records in CSV file ({} total lines)", recordCount, lines.length);
            return recordCount;
        } catch (Exception e) {
            log.warn("Could not count records in CSV file, using default count", e);
            return 0; // Return 0 if cannot count
        }
    }
    
    // ✅ BATCH STATUS API - Add this method
    @GetMapping("/status/{batchLogId}")
    public ResponseEntity<?> getBatchStatus(@PathVariable Long batchLogId) {
        try {
            log.info("Fetching batch status for ID: {}", batchLogId);
            
            EmployeeBatchLog batchLog = batchLogRepository.findById(batchLogId)
                    .orElseThrow(() -> new RuntimeException("Batch log not found with ID: " + batchLogId));
            
            // Create response map (you can also create a DTO if preferred)
            Map<String, Object> response = Map.of(
                "id", batchLog.getId().toString(),
                "status", batchLog.getStatus(),
                "totalCount", batchLog.getTotalCount(),
                "processedCount", batchLog.getProcessedCount(),
                "failedCount", batchLog.getFailedCount(),
                "createdAt", batchLog.getCreatedAt()
            );
            
            log.info("Batch status for ID {}: {}", batchLogId, response);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Batch log not found for ID: {}", batchLogId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Batch log not found with ID: " + batchLogId));
        } catch (Exception e) {
            log.error("Error fetching batch status for ID: {}", batchLogId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching batch status: " + e.getMessage()));
        }
    }
    
    // ✅ OPTIONAL: Get all batch logs
    @GetMapping("/all")
    public ResponseEntity<?> getAllBatchLogs() {
        try {
            var batchLogs = batchLogRepository.findAll();
            log.info("Fetched {} batch logs", batchLogs.size());
            return ResponseEntity.ok(batchLogs);
        } catch (Exception e) {
            log.error("Error fetching all batch logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching batch logs: " + e.getMessage()));
        }
    }
}