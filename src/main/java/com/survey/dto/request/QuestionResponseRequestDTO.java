package com.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionResponseRequestDTO {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Answer cannot be null")
    private String answerText;
}
