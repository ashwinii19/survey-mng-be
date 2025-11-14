package com.survey.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentStatsDTO {

    private String departmentName;
    private long totalEmployees;
    private long submitted;
    private long pending;
}
