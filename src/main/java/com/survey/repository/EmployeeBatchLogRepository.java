package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.survey.entity.EmployeeBatchLog;

public interface EmployeeBatchLogRepository extends JpaRepository<EmployeeBatchLog, Long> {
}
