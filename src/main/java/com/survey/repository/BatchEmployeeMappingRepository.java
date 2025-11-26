package com.survey.repository;

import com.survey.entity.BatchEmployeeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BatchEmployeeMappingRepository extends JpaRepository<BatchEmployeeMapping, Long> {
    
    // Find all mappings for a specific batch
    List<BatchEmployeeMapping> findByBatchLogId(Long batchLogId);
    
    // Get just employee emails for a batch
    @Query("SELECT bem.employeeEmail FROM BatchEmployeeMapping bem WHERE bem.batchLogId = :batchLogId")
    List<String> findEmployeeEmailsByBatchLogId(@Param("batchLogId") Long batchLogId);
    
    // Check if mapping already exists
    boolean existsByBatchLogIdAndEmployeeEmail(Long batchLogId, String employeeEmail);
    
    // Count employees in a batch
    long countByBatchLogId(Long batchLogId);
    
    // Find batches for an employee (reverse lookup)
    List<BatchEmployeeMapping> findByEmployeeEmail(String employeeEmail);
}