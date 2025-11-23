package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestionStatsDTO {
    private Long questionId;
    private String questionText;
    private String questionType;
    private int totalResponses;

    private Map<String, Integer> optionCounts;

    private List<String> textAnswers;
    
    private List<QuestionAnswerResponseDTO> responses;

}
