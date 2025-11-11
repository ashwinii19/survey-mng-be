package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SurveySubmissionResponseDTO {
    private Long id;
    private Long surveyId;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime submittedAt;
    private List<QuestionAnswerResponseDTO> answers;
}
