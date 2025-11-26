package com.survey.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
	//Optional<Department> findByName(String name);
	Department findByName(String name);

}
