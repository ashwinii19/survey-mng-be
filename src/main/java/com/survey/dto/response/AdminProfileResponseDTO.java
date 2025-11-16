package com.survey.dto.response;

import lombok.Data;

@Data
public class AdminProfileResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String profileImage; // optional
}
