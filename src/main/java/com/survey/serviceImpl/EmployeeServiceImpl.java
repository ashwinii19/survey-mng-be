//
//
//
//package com.survey.serviceImpl;
//
//import com.survey.dto.request.EmployeeRequestDTO;
//import com.survey.dto.response.DepartmentResponseDTO;
//import com.survey.dto.response.EmployeeResponseDTO;
//import com.survey.entity.Department;
//import com.survey.entity.Employee;
//import com.survey.repository.DepartmentRepository;
//import com.survey.repository.EmployeeRepository;
//import com.survey.service.EmployeeService;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class EmployeeServiceImpl implements EmployeeService {
//
//    private final EmployeeRepository employeeRepository;
//    private final DepartmentRepository departmentRepository;
//
//    private EmployeeResponseDTO mapToResponse(Employee employee) {
//        EmployeeResponseDTO dto = new EmployeeResponseDTO();
//        dto.setId(employee.getId());
//        dto.setEmployeeId(employee.getEmployeeId());
//        dto.setName(employee.getName());
//        dto.setEmail(employee.getEmail());
//        dto.setPosition(employee.getPosition());
//        dto.setStatus(employee.getStatus());
//        dto.setJoinDate(employee.getJoinDate());
//
//        if (employee.getDepartment() != null) {
//            DepartmentResponseDTO deptDto = new DepartmentResponseDTO();
//            deptDto.setId(employee.getDepartment().getId());
//            deptDto.setName(employee.getDepartment().getName());
//            dto.setDepartment(deptDto);
//        }
//        return dto;
//    }
//
//    @Override
//    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
//        // Check if employee with same email already exists
//        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
//            throw new RuntimeException("Employee with email " + dto.getEmail() + " already exists");
//        }
//        
//        // Check if employee with same employeeId already exists
//        if (employeeRepository.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
//            throw new RuntimeException("Employee with ID " + dto.getEmployeeId() + " already exists");
//        }
//
//        Employee emp = new Employee();
//        emp.setEmployeeId(dto.getEmployeeId());
//        emp.setName(dto.getName());
//        emp.setEmail(dto.getEmail());
//        emp.setPosition(dto.getPosition());
//        emp.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
//        emp.setJoinDate(dto.getJoinDate());
//
//        Department department = departmentRepository.findById(dto.getDepartmentId())
//                .orElseThrow(() -> new RuntimeException("Department not found"));
//        emp.setDepartment(department);
//
//        Employee savedEmployee = employeeRepository.save(emp);
//        return mapToResponse(savedEmployee);
//    }
//
//    @Override
//    public List<EmployeeResponseDTO> getAllEmployees() {
//        return employeeRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
//        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
//
//        // Check if email is being changed and if new email already exists
//        if (!emp.getEmail().equals(dto.getEmail())) {
//            if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
//                throw new RuntimeException("Employee with email " + dto.getEmail() + " already exists");
//            }
//        }
//
//        // Check if employeeId is being changed and if new employeeId already exists
//        if (!emp.getEmployeeId().equals(dto.getEmployeeId())) {
//            if (employeeRepository.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
//                throw new RuntimeException("Employee with ID " + dto.getEmployeeId() + " already exists");
//            }
//        }
//
//        emp.setEmployeeId(dto.getEmployeeId());
//        emp.setName(dto.getName());
//        emp.setEmail(dto.getEmail());
//        emp.setPosition(dto.getPosition());
//        emp.setStatus(dto.getStatus());
//        emp.setJoinDate(dto.getJoinDate());
//
//        Department department = departmentRepository.findById(dto.getDepartmentId())
//                .orElseThrow(() -> new RuntimeException("Department not found"));
//        emp.setDepartment(department);
//
//        Employee updatedEmployee = employeeRepository.save(emp);
//        return mapToResponse(updatedEmployee);
//    }
//
//    @Override
//    public List<EmployeeResponseDTO> getEmployeesByDepartmentName(String departmentName) {
//        return employeeRepository.findByDepartment_Name(departmentName).stream().map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public EmployeeResponseDTO getEmployeeById(String employeeId) {
//        Employee emp = employeeRepository.findByEmployeeId(employeeId)
//                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
//        return mapToResponse(emp);
//    }
//
//    @Override
//    public EmployeeResponseDTO updateEmployee(String employeeId, EmployeeRequestDTO dto) {
//        Employee emp = employeeRepository.findByEmployeeId(employeeId)
//                .orElseThrow(() -> new RuntimeException("Employee not found"));
//
//        // Check if email is being changed and if new email already exists
//        if (!emp.getEmail().equals(dto.getEmail())) {
//            if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
//                throw new RuntimeException("Employee with email " + dto.getEmail() + " already exists");
//            }
//        }
//
//        emp.setName(dto.getName());
//        emp.setEmail(dto.getEmail());
//        emp.setPosition(dto.getPosition());
//        emp.setStatus(dto.getStatus());
//        emp.setJoinDate(dto.getJoinDate());
//
//        Department department = departmentRepository.findById(dto.getDepartmentId())
//                .orElseThrow(() -> new RuntimeException("Department not found"));
//        emp.setDepartment(department);
//
//        Employee updatedEmployee = employeeRepository.save(emp);
//        return mapToResponse(updatedEmployee);
//    }
//
//    @Override
//    public void deleteEmployee(String employeeId) {
//        Employee emp = employeeRepository.findByEmployeeId(employeeId)
//                .orElseThrow(() -> new RuntimeException("Employee not found"));
//        emp.setStatus("INACTIVE");
//        employeeRepository.save(emp);
//    }
//
//    @Override
//    public EmployeeResponseDTO getEmployeeByEmail(String email) {
//        Employee emp = employeeRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
//        return mapToResponse(emp);
//    }
//
//    @Override
//    public List<EmployeeResponseDTO> getEmployeesByStatus(String status) {
//        return employeeRepository.findAll().stream()
//                .filter(emp -> status.equalsIgnoreCase(emp.getStatus()))
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<EmployeeResponseDTO> getEmployeesByEmails(List<String> emails) {
//        // 🟡 NEW METHOD: Get employees by list of emails (for batch processing)
//        List<Employee> employees = employeeRepository.findByEmailIn(emails);
//        return employees.stream().map(this::mapToResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    public long getActiveEmployeeCount() {
//        return employeeRepository.findAll().stream()
//                .filter(emp -> "ACTIVE".equalsIgnoreCase(emp.getStatus()))
//                .count();
//    }
//
//    @Override
//    public List<EmployeeResponseDTO> searchEmployees(String searchTerm) {
//        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
//        return employeeRepository.findAll().stream()
//                .filter(emp -> 
//                    emp.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
//                    emp.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
//                    emp.getEmployeeId().toLowerCase().contains(searchTerm.toLowerCase()) ||
//                    (emp.getPosition() != null && emp.getPosition().toLowerCase().contains(searchTerm.toLowerCase()))
//                )
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void bulkUpdateStatus(List<String> employeeIds, String status) {
//        List<Employee> employees = employeeRepository.findAll().stream()
//                .filter(emp -> employeeIds.contains(emp.getEmployeeId()))
//                .collect(Collectors.toList());
//        
//        for (Employee emp : employees) {
//            emp.setStatus(status);
//        }
//        
//        employeeRepository.saveAll(employees);
//    }
//}









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
        // Check if employee with same email already exists
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Employee with email " + dto.getEmail() + " already exists");
        }
        
        // Check if employee with same employeeId already exists
        if (employeeRepository.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
            throw new RuntimeException("Employee with ID " + dto.getEmployeeId() + " already exists");
        }

        // 🟡 CHANGE: Find department by NAME instead of ID
        Department department = departmentRepository.findByName(dto.getDepartmentName());
        if (department == null) {
            throw new RuntimeException("Department not found with name: " + dto.getDepartmentName());
        }

        Employee emp = new Employee();
        emp.setEmployeeId(dto.getEmployeeId());
        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPosition(dto.getPosition());
        emp.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        emp.setJoinDate(dto.getJoinDate());
        emp.setDepartment(department);

        Employee savedEmployee = employeeRepository.save(emp);
        return mapToResponse(savedEmployee);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check if email is being changed and if new email already exists
        if (!emp.getEmail().equals(dto.getEmail())) {
            if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Employee with email " + dto.getEmail() + " already exists");
            }
        }

        // Check if employeeId is being changed and if new employeeId already exists
        if (!emp.getEmployeeId().equals(dto.getEmployeeId())) {
            if (employeeRepository.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
                throw new RuntimeException("Employee with ID " + dto.getEmployeeId() + " already exists");
            }
        }

        // 🟡 CHANGE: Find department by NAME instead of ID
        Department department = departmentRepository.findByName(dto.getDepartmentName());
        if (department == null) {
            throw new RuntimeException("Department not found with name: " + dto.getDepartmentName());
        }

        emp.setEmployeeId(dto.getEmployeeId());
        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPosition(dto.getPosition());
        emp.setStatus(dto.getStatus());
        emp.setJoinDate(dto.getJoinDate());
        emp.setDepartment(department);

        Employee updatedEmployee = employeeRepository.save(emp);
        return mapToResponse(updatedEmployee);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartmentName(String departmentName) {
        return employeeRepository.findByDepartment_Name(departmentName).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(String employeeId) {
        Employee emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        return mapToResponse(emp);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(String employeeId, EmployeeRequestDTO dto) {
        Employee emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check if email is being changed and if new email already exists
        if (!emp.getEmail().equals(dto.getEmail())) {
            if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Employee with email " + dto.getEmail() + " already exists");
            }
        }

        // 🟡 CHANGE: Find department by NAME instead of ID
        Department department = departmentRepository.findByName(dto.getDepartmentName());
        if (department == null) {
            throw new RuntimeException("Department not found with name: " + dto.getDepartmentName());
        }

        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPosition(dto.getPosition());
        emp.setStatus(dto.getStatus());
        emp.setJoinDate(dto.getJoinDate());
        emp.setDepartment(department);

        Employee updatedEmployee = employeeRepository.save(emp);
        return mapToResponse(updatedEmployee);
    }

    @Override
    public void deleteEmployee(String employeeId) {
        Employee emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        emp.setStatus("INACTIVE");
        employeeRepository.save(emp);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByEmail(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
        return mapToResponse(emp);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByStatus(String status) {
        return employeeRepository.findAll().stream()
                .filter(emp -> status.equalsIgnoreCase(emp.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByEmails(List<String> emails) {
        // 🟡 NEW METHOD: Get employees by list of emails (for batch processing)
        List<Employee> employees = employeeRepository.findByEmailIn(emails);
        return employees.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public long getActiveEmployeeCount() {
        return employeeRepository.findAll().stream()
                .filter(emp -> "ACTIVE".equalsIgnoreCase(emp.getStatus()))
                .count();
    }

    @Override
    public List<EmployeeResponseDTO> searchEmployees(String searchTerm) {
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        return employeeRepository.findAll().stream()
                .filter(emp -> 
                    emp.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    emp.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    emp.getEmployeeId().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (emp.getPosition() != null && emp.getPosition().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void bulkUpdateStatus(List<String> employeeIds, String status) {
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(emp -> employeeIds.contains(emp.getEmployeeId()))
                .collect(Collectors.toList());
        
        for (Employee emp : employees) {
            emp.setStatus(status);
        }
        
        employeeRepository.saveAll(employees);
    }
}
