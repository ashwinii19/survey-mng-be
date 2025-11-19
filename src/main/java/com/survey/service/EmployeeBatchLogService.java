package com.survey.service;

import com.survey.entity.EmployeeBatchLog;
import com.survey.repository.EmployeeBatchLogRepository;
import com.survey.dto.response.EmployeeBatchLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeBatchLogService {
    
    private final EmployeeBatchLogRepository employeeBatchLogRepository;
    
    @Transactional(readOnly = true)
    public EmployeeBatchLogResponse getBatchLogStatus(Long batchLogId) {
        log.info("Fetching employee batch log status for ID: {}", batchLogId);
        
        EmployeeBatchLog batchLog = employeeBatchLogRepository.findById(batchLogId)
                .orElseThrow(() -> new RuntimeException("Employee batch log not found with id: " + batchLogId));
        
        return EmployeeBatchLogResponse.fromEntity(batchLog);
    }
    
    public void updateBatchLogStatus(Long batchLogId, String status, Integer processedCount, Integer failedCount) {
        log.info("Updating batch log {}: status={}, processed={}, failed={}", 
                batchLogId, status, processedCount, failedCount);
        
        EmployeeBatchLog batchLog = employeeBatchLogRepository.findById(batchLogId)
                .orElseThrow(() -> new RuntimeException("Employee batch log not found with id: " + batchLogId));
        
        batchLog.setStatus(status);
        batchLog.setProcessedCount(processedCount);
        batchLog.setFailedCount(failedCount);
        
        employeeBatchLogRepository.save(batchLog);
    }
    
    public Long createEmployeeBatchLog(Integer totalCount) {
        log.info("Creating new employee batch log with total count: {}", totalCount);
        
        EmployeeBatchLog batchLog = new EmployeeBatchLog();
        batchLog.setTotalCount(totalCount);
        batchLog.setProcessedCount(0);
        batchLog.setFailedCount(0);
        batchLog.setStatus("PENDING");
        
        EmployeeBatchLog saved = employeeBatchLogRepository.save(batchLog);
        return saved.getId();
    }
    
    @Transactional(readOnly = true)
    public List<EmployeeBatchLogResponse> getAllBatchLogs() {
        log.info("Fetching all employee batch logs");
        
        return employeeBatchLogRepository.findAll()
                .stream()
                .map(EmployeeBatchLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}