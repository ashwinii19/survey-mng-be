package com.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminResponseDTO {
    private String token;
    private String name;
}

