package com.survey.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyRequestDTO {
	private String title;
	private String description;
	private boolean published;
	private Long targetDepartmentId;
	private List<QuestionRequestDTO> questions;
}
