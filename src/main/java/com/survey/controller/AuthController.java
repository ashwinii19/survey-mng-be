package com.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.survey.dto.request.AdminLoginDTO;
import com.survey.dto.request.ForgotPasswordRequestDTO;
import com.survey.dto.request.ResetPasswordDTO;
import com.survey.dto.request.VerifyOtpDTO;
import com.survey.dto.response.AdminResponseDTO;
import com.survey.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AdminResponseDTO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        authService.sendResetOtp(dto.getEmail());
        return ResponseEntity.ok("OTP sent to email.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        boolean valid = authService.verifyOtp(dto.getEmail(), dto.getOtp());
        return valid ? ResponseEntity.ok("OTP Verified") :
                ResponseEntity.badRequest().body("Invalid or expired OTP");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto.getEmail(), dto.getNewPassword());
        return ResponseEntity.ok("Password updated successfully.");
    }

}
