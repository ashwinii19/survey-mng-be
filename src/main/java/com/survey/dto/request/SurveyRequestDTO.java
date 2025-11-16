package com.survey.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyRequestDTO {

    @NotBlank(message = "Survey title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Survey description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

//    private boolean published;
    private boolean draft;


    private String targetDepartmentName;

//    @NotEmpty(message = "Survey must contain at least one question")
    private List<@Valid QuestionRequestDTO> questions;
}
