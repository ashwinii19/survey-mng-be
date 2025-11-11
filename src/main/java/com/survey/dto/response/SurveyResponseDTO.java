package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SurveyResponseDTO {
	private Long id;
	private String title;
	private String description;
	private boolean published;
	private String formLink; 
	private LocalDateTime createdAt;
	private LocalDateTime publishedAt;
	private DepartmentResponseDTO targetDepartment;
	private List<QuestionResponseDTO> questions;
}