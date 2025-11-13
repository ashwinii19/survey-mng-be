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

    
}
