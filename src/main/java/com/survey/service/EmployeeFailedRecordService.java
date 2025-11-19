package com.survey.service;

import com.survey.entity.EmployeeFailedRecord;
import com.survey.repository.EmployeeFailedRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeFailedRecordService {
    
    private final EmployeeFailedRecordRepository employeeFailedRecordRepository;
    
    public List<EmployeeFailedRecord> getAllFailedRecords() {
        return employeeFailedRecordRepository.findAll();
    }
    
    public List<EmployeeFailedRecord> getFailedRecordsByBatchLogId(Long batchLogId) {
        return employeeFailedRecordRepository.findByBatchLogId(batchLogId);
    }
    
    public EmployeeFailedRecord getFailedRecordById(Long id) {
        return employeeFailedRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Failed record not found with id: " + id));
    }
}