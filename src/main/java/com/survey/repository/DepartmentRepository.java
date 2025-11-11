package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.survey.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
