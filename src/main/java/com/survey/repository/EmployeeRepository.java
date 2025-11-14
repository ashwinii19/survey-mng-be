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
    
    List<Employee> findByDepartmentId(Long departmentId);
    
    Optional<Employee> findByEmployeeId(String employeeId);
    
    // Check if email exists
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    // Check if employee ID exists
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.employeeId = :employeeId")
    boolean existsByEmployeeId(@Param("employeeId") String employeeId);
    
    // Find by email
    Optional<Employee> findByEmail(String email);
    
    // Count employees by department
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);
    
    // Find employees by status
    List<Employee> findByStatus(String status);
    
    // Find employees by position
    List<Employee> findByPosition(String position);
    
    // Find employees by department and status
    List<Employee> findByDepartmentIdAndStatus(Long departmentId, String status);
}