package com.survey.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardResponseDTO {
    private long totalEmployees;
    private long totalSurveys;
    private long totalSubmitted;
    private long totalPending;

    private List<DepartmentStatsDTO> departmentStats;
    private SurveyStatsDTO surveyStats;
    
    private List<String> submittedEmployees; // list of employee names / display strings
    private List<String> pendingEmployees;
}
