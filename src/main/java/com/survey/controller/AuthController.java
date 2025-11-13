package com.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.survey.dto.request.AdminLoginDTO;
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
}
