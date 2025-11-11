package com.survey.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class QuestionResponseDTO {
    private Long id;
    private String text;
    private String type;
    private boolean required;
    private List<String> options;
}
