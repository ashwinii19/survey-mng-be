package com.survey.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyResponseDTO {
    private Long id;
    private String title;
    private String description;
    private boolean published;
    private boolean editable;
    private String formLink;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    private List<String> targetDepartments;         // list of names
    private List<QuestionResponseDTO> questions;    // all questions
    private String targetDepartmentName;            // "ALL" / "HR,IT"
}
