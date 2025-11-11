package com.survey.controller;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.dto.response.EmployeeResponseDTO;
import com.survey.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin("*")  // Allows requests from frontend (React, Angular, etc.)
public class EmployeeController {

    private final EmployeeService employeeService;

    
    @PostMapping
    public EmployeeResponseDTO createEmployee(@RequestBody EmployeeRequestDTO dto) {
        return employeeService.createEmployee(dto);
    }

   
    @GetMapping
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeService.getAllEmployees();
    }


    @GetMapping("/{id}")
    public EmployeeResponseDTO getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    
    @PutMapping("/{id}")
    public EmployeeResponseDTO updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
        return employeeService.updateEmployee(id, dto);
    }

   
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }

    
    @GetMapping("/submitted/{status}")
    public List<EmployeeResponseDTO> getEmployeesBySubmissionStatus(@PathVariable boolean status) {
        return employeeService.getEmployeesBySubmissionStatus(status);
    }

   
    @GetMapping("/count/{status}")
    public long countEmployeesBySubmissionStatus(@PathVariable boolean status) {
        return employeeService.countEmployeesBySubmissionStatus(status);
    }

    @GetMapping("/department/{name}")
    public List<EmployeeResponseDTO> getEmployeesByDepartmentName(@PathVariable String name) {
        return employeeService.getEmployeesByDepartmentName(name);
    }

   
    @GetMapping("/department/{name}/submitted/{status}")
    public List<EmployeeResponseDTO> getEmployeesByDepartmentAndSubmission(
            @PathVariable String name,
            @PathVariable boolean status) {
        return employeeService.getEmployeesByDepartmentAndSubmission(name, status);
    }
}
