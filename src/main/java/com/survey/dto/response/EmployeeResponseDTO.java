package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponseDTO {
    private Long id;
    private String employeeId;
    private String name;
    private String email;
    private String position;
    private String status;
    private String joinDate;
    private DepartmentResponseDTO department;
    private boolean submitted; 
}
