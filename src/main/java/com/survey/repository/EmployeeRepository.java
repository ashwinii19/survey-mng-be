package com.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.survey.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	List<Employee> findBySubmitted(boolean submitted);

	long countBySubmitted(boolean submitted);

	List<Employee> findByDepartment_Name(String departmentName);

    List<Employee> findByDepartment_NameAndSubmitted(String departmentName, boolean submitted);
    
    
    
    List<Employee> findByDepartmentId(Long departmentId);
    
    Optional<Employee> findByEmployeeId(String employeeId);

}
