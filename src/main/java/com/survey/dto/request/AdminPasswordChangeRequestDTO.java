package com.survey.dto.request;

import lombok.Data;

@Data
public class AdminPasswordChangeRequestDTO {
    private String oldPassword;
    private String newPassword;
}

