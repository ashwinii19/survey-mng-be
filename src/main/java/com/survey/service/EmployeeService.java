package com.survey.service;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.dto.response.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

   
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
   
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    
    

    
   
    List<EmployeeResponseDTO> getEmployeesByDepartmentName(String departmentName);
    
	EmployeeResponseDTO getEmployeeById(String employeeId);
	EmployeeResponseDTO updateEmployee(String employeeId, EmployeeRequestDTO dto);

	void deleteEmployee(String employeeId);
	
	
	
	// 🟡 ADD THESE METHODS FOR BATCH PROCESSING AND COMPLETENESS
    EmployeeResponseDTO getEmployeeByEmail(String email);
    List<EmployeeResponseDTO> getEmployeesByStatus(String status);
    List<EmployeeResponseDTO> getEmployeesByEmails(List<String> emails);
    long getActiveEmployeeCount();
    List<EmployeeResponseDTO> searchEmployees(String searchTerm);
    void bulkUpdateStatus(List<String> employeeIds, String status);

    
}
