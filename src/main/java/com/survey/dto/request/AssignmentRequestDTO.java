package com.survey.dto.request;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentRequestDTO {
	private Long surveyId;
	private List<Long> departmentIds; 
	private List<Long> employeeIds;
	private LocalDateTime dueDate;
}
