package com.survey.controller;

import com.survey.dto.response.EmployeeFailedRecordResponse;
import com.survey.entity.EmployeeFailedRecord;
import com.survey.service.EmployeeFailedRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/failed-records")
@RequiredArgsConstructor
public class EmployeeFailedRecordController {
    
    private final EmployeeFailedRecordService employeeFailedRecordService;
    
   
    @GetMapping
    public ResponseEntity<List<EmployeeFailedRecordResponse>> getAllFailedRecords() {
        List<EmployeeFailedRecordResponse> responses = employeeFailedRecordService.getAllFailedRecords()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
 
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeFailedRecordResponse> getFailedRecordById(@PathVariable Long id) {
        EmployeeFailedRecord record = employeeFailedRecordService.getFailedRecordById(id);
        return ResponseEntity.ok(convertToResponse(record));
    }
    
    
    @GetMapping("/batch/{batchLogId}")
    public ResponseEntity<List<EmployeeFailedRecordResponse>> getFailedRecordsByBatchLogId(@PathVariable Long batchLogId) {
        List<EmployeeFailedRecordResponse> responses = employeeFailedRecordService.getFailedRecordsByBatchLogId(batchLogId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
   
    private EmployeeFailedRecordResponse convertToResponse(EmployeeFailedRecord record) {
        EmployeeFailedRecordResponse response = new EmployeeFailedRecordResponse();
        response.setId(record.getId());
        response.setEmployeeData(record.getEmployeeData());
        response.setErrorMessage(record.getErrorMessage());
        response.setReason(record.getReason());
        response.setFailedAt(record.getFailedAt());
        response.setBatchLogId(record.getBatchLogId());
        return response;
    }
}