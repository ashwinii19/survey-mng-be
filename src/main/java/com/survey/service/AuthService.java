package com.survey.service;

import com.survey.dto.request.AdminLoginDTO;
import com.survey.dto.response.AdminResponseDTO;

public interface AuthService {

	AdminResponseDTO login(AdminLoginDTO loginDTO);

	void createDefaultAdmin();

	void sendResetOtp(String email);

	boolean verifyOtp(String email, String otp);

	void resetPassword(String email, String newPassword);

}
