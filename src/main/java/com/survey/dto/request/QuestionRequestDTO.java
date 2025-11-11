package com.survey.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequestDTO {
	private String text;
	private String type;
	private List<String> options;
	private boolean required;
}
