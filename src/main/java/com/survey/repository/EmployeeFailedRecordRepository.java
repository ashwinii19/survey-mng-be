package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.survey.entity.EmployeeFailedRecord;
import java.util.List;

public interface EmployeeFailedRecordRepository extends JpaRepository<EmployeeFailedRecord, Long> {
    List<EmployeeFailedRecord> findByBatchLogId(Long batchLogId);
}


