package com.survey.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class QuestionRequestDTO {

    @NotBlank(message = "Question text is required")
    private String text;

    @NotBlank(message = "Question type is required")
    private String type;

    private List<@NotBlank(message = "Option cannot be blank") String> options;

    private boolean required;
}
