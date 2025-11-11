package com.survey.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseRequestDTO {
    private Long questionId;
    private String answerText;
}
