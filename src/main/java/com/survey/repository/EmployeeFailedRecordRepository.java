package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.survey.entity.EmployeeFailedRecord;
import java.util.List;

public interface EmployeeFailedRecordRepository extends JpaRepository<EmployeeFailedRecord, Long> {
    List<EmployeeFailedRecord> findByBatchLogId(Long batchLogId);
    @Query("SELECT COUNT(f) FROM EmployeeFailedRecord f WHERE f.batchLogId = :batchLogId")
    long countByBatchLogId(@Param("batchLogId") Long batchLogId);
}


