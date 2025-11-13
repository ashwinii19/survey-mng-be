package com.survey.dto.dashboard;


import java.util.List;

import lombok.Data;

@Data
public class DashboardDTO {

    private List<RecentSurveyDTO> recentSurveys;

    private int totalEmployees;
    private int submitted;
    private int pending;

    private List<ChartPoint> departmentBar;
    private List<ChartPoint> departmentDonut;
}
