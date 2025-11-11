package com.survey.serviceImpl;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.dto.response.DepartmentResponseDTO;
import com.survey.dto.response.EmployeeResponseDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.service.EmployeeService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

   
    private EmployeeResponseDTO mapToResponse(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(employee.getId());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setPosition(employee.getPosition());
        dto.setStatus(employee.getStatus());
        dto.setJoinDate(employee.getJoinDate());
        dto.setSubmitted(employee.isSubmitted());

        if (employee.getDepartment() != null) {
            DepartmentResponseDTO deptDto = new DepartmentResponseDTO();
            deptDto.setId(employee.getDepartment().getId());
            deptDto.setName(employee.getDepartment().getName());
            dto.setDepartment(deptDto);
        }
        return dto;
    }

   
    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        Employee emp = new Employee();
        emp.setEmployeeId(dto.getEmployeeId());
        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPosition(dto.getPosition());
        emp.setStatus(dto.getStatus());
        emp.setJoinDate(dto.getJoinDate());
        emp.setSubmitted(false);

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        emp.setDepartment(department);

        Employee savedEmployee = employeeRepository.save(emp);
        return mapToResponse(savedEmployee);
    }

   
    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapToResponse(emp);
    }

    
    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        emp.setEmployeeId(dto.getEmployeeId());
        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPosition(dto.getPosition());
        emp.setStatus(dto.getStatus());
        emp.setJoinDate(dto.getJoinDate());

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        emp.setDepartment(department);

        Employee updatedEmployee = employeeRepository.save(emp);
        return mapToResponse(updatedEmployee);
    }

   
    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    
    @Override
    public List<EmployeeResponseDTO> getEmployeesBySubmissionStatus(boolean submitted) {
        return employeeRepository.findBySubmitted(submitted)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    @Override
    public long countEmployeesBySubmissionStatus(boolean submitted) {
        return employeeRepository.countBySubmitted(submitted);
    }

    
    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartmentName(String departmentName) {
        return employeeRepository.findByDepartment_Name(departmentName)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

   
    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartmentAndSubmission(String departmentName, boolean submitted) {
        return employeeRepository.findByDepartment_NameAndSubmitted(departmentName, submitted)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
