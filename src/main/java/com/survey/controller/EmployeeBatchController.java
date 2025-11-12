package com.survey.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/employees/batch")
@RequiredArgsConstructor
@Slf4j
public class EmployeeBatchController {

    private final JobLauncher jobLauncher;
    private final Job importEmployeeJob;

    // ✅ File upload endpoint
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> importEmployees(
            @RequestParam("file") MultipartFile file) {

        Path tempFilePath = null;
        try {
            // 1️⃣ Save uploaded CSV to a temporary file
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

            // 2️⃣ Build job parameters (file path + timestamp)
            var jobParams = new JobParametersBuilder()
                    .addString("filePath", tempFilePath.toAbsolutePath().toString())
                    .addLong("time", System.currentTimeMillis()) // ensures a unique job instance
                    .toJobParameters();

            // 3️⃣ Launch the batch job
            jobLauncher.run(importEmployeeJob, jobParams);

            return ResponseEntity.ok(Map.of("message", "Employee batch import job started successfully."));

        } catch (Exception e) {
            log.error("Batch import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Employee batch import failed: " + e.getMessage()));
        } finally {
            // 4️⃣ Clean up temp file
            if (tempFilePath != null) {
                try {
                    Files.delete(tempFilePath);
                } catch (Exception e) {
                    log.warn("Could not delete temporary file: {}", tempFilePath, e);
                }
            }
        }
    }
}
