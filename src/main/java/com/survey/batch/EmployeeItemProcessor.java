package com.survey.batch;

import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeItemProcessor implements ItemProcessor<EmployeeRequestDTO, Employee> {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public Employee process(EmployeeRequestDTO dto) throws Exception {
        Employee employee = new Employee();

        // map manually to avoid ModelMapper confusion
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPosition(dto.getPosition());
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        employee.setJoinDate(dto.getJoinDate());

        // Attach department if exists
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        return employee;
    }
}
