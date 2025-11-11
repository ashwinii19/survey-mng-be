package com.survey.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRequestDTO {
    private String employeeId;
    private String name;
    private String email;
    private String position;
    private String status;
    private String joinDate;
    private Long departmentId; 
}