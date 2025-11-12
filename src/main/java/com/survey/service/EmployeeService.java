package com.survey.service;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.dto.response.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

   
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
   
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    
    

    
    List<EmployeeResponseDTO> getEmployeesBySubmissionStatus(boolean submitted);
    long countEmployeesBySubmissionStatus(boolean submitted);
    List<EmployeeResponseDTO> getEmployeesByDepartmentName(String departmentName);
    List<EmployeeResponseDTO> getEmployeesByDepartmentAndSubmission(String departmentName, boolean submitted);
	EmployeeResponseDTO getEmployeeById(String employeeId);
	EmployeeResponseDTO updateEmployee(String employeeId, EmployeeRequestDTO dto);

	void deleteEmployee(String employeeId);

    
}
