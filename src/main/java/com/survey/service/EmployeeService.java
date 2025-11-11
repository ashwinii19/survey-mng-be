package com.survey.service;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.dto.response.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

   
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
    EmployeeResponseDTO getEmployeeById(Long id);
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    void deleteEmployee(Long id);

    
    List<EmployeeResponseDTO> getEmployeesBySubmissionStatus(boolean submitted);
    long countEmployeesBySubmissionStatus(boolean submitted);
    List<EmployeeResponseDTO> getEmployeesByDepartmentName(String departmentName);
    List<EmployeeResponseDTO> getEmployeesByDepartmentAndSubmission(String departmentName, boolean submitted);
}
