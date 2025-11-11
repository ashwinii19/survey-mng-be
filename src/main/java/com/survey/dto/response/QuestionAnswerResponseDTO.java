package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionAnswerResponseDTO {
    private Long questionId;
    private String questionText;
    private String answerText;
}
