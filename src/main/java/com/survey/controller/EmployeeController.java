//package com.survey.controller;
//
//import com.survey.dto.request.EmployeeRequestDTO;
//import com.survey.dto.response.EmployeeResponseDTO;
//import com.survey.service.EmployeeService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/employees")
//@RequiredArgsConstructor
//@CrossOrigin("*")  
//public class EmployeeController {
//
//    private final EmployeeService employeeService;
//
//    
//    @PostMapping
//    public EmployeeResponseDTO createEmployee(@RequestBody EmployeeRequestDTO dto) {
//        return employeeService.createEmployee(dto);
//    }
//
//   
//    @GetMapping
//    public List<EmployeeResponseDTO> getAllEmployees() {
//        return employeeService.getAllEmployees();
//    }
//
//
//   
//
//
//    @GetMapping("/{employeeId}")
//    public EmployeeResponseDTO getEmployeeById(@PathVariable String employeeId) {
//        return employeeService.getEmployeeById(employeeId);
//    }
//
//    
//    @PutMapping("/{employeeId}")
//    public EmployeeResponseDTO updateEmployee(@PathVariable String employeeId, @RequestBody EmployeeRequestDTO dto) {
//        return employeeService.updateEmployee(employeeId, dto);
//    }
//
//   
//    @DeleteMapping("/{employeeId}")
//    public void deleteEmployee(@PathVariable String employeeId) {
//        employeeService.deleteEmployee(employeeId);
//    }
//
//    
//
//
//    @GetMapping("/department/{name}")
//    public List<EmployeeResponseDTO> getEmployeesByDepartmentName(@PathVariable String name) {
//        return employeeService.getEmployeesByDepartmentName(name);
//    }
//
//   
//
//}


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

    @GetMapping("/{employeeId}")
    public EmployeeResponseDTO getEmployeeById(@PathVariable String employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponseDTO updateEmployee(@PathVariable String employeeId, @RequestBody EmployeeRequestDTO dto) {
        return employeeService.updateEmployee(employeeId, dto);
    }

    @DeleteMapping("/{employeeId}")
    public void deleteEmployee(@PathVariable String employeeId) {
        employeeService.deleteEmployee(employeeId);
    }

    @GetMapping("/department/{name}")
    public List<EmployeeResponseDTO> getEmployeesByDepartmentName(@PathVariable String name) {
        return employeeService.getEmployeesByDepartmentName(name);
    }
}