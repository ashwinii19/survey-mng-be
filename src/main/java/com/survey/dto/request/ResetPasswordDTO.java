package com.survey.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
	private String email;
	private String newPassword;
}
