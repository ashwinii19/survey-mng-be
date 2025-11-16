package com.survey.service;

import org.springframework.web.multipart.MultipartFile;

import com.survey.dto.request.AdminLoginDTO;
import com.survey.dto.request.AdminUpdateRequestDTO;
import com.survey.dto.response.AdminProfileResponseDTO;
import com.survey.dto.response.AdminResponseDTO;

public interface AuthService {

	AdminResponseDTO login(AdminLoginDTO loginDTO);

	void createDefaultAdmin();

	void sendResetOtp(String email);

	boolean verifyOtp(String email, String otp);

	void resetPassword(String email, String newPassword);
	
	AdminProfileResponseDTO updateProfile(String email, AdminUpdateRequestDTO dto);

	void changePassword(String email, String currentPassword, String newPassword);

	String uploadProfileImage(String email, MultipartFile file) throws Exception;


}
