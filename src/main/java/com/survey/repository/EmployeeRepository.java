package com.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.survey.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	List<Employee> findByDepartment_Name(String departmentName);

    
    Optional<Employee> findByEmployeeId(String employeeId);
    
    int countByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentId(Long departmentId);

    Optional<Employee> findByEmail(String email);
    
//    Employee findByEmployeeId(String employeeId);

}
