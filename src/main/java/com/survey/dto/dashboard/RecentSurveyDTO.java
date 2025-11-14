package com.survey.dto.dashboard;

import lombok.Data;

@Data
public class RecentSurveyDTO {
    private Long id;
    private String title;
    private String department;
    private String publishedDate;
    private int completion;
    private String status;
}
