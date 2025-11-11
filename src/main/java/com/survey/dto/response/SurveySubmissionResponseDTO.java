package com.survey.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveySubmissionResponseDTO {
	private Long surveyId;
	private String surveyTitle;
	private String departmentName;

	private long totalEmployees;
	private long submittedCount;
	private long pendingCount;

	private List<String> submittedEmployees;
	private List<String> pendingEmployees;
}
