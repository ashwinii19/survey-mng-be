package com.survey.dto.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardSummaryDTO {

    private int totalSurveys;
    private int totalResponses;
    private int pendingResponses;

    private List<ChartPoint> departmentBarChart;
    private List<ChartPoint> departmentDonutChart;
}
