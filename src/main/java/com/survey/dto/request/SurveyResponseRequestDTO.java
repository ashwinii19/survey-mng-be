package com.survey.dto.request;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponseRequestDTO {
    private Long surveyId;
    private Long employeeId;
    private List<QuestionResponseRequestDTO> answers;
}

