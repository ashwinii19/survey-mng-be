package com.survey.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyStatsDTO {
    private Long surveyId;
    private String surveyTitle;
    private long totalEmployees;
    private long totalSubmitted;
    private long totalPending;
}
