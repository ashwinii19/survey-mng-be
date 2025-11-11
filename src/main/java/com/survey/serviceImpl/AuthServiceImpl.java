package com.survey.serviceImpl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.survey.dto.request.AdminLoginDTO;
import com.survey.dto.response.AdminResponseDTO;
import com.survey.entity.Admin;
import com.survey.repository.AdminRepository;
import com.survey.security.JwtTokenProvider;
import com.survey.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AdminResponseDTO login(AdminLoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(), loginDTO.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        Admin admin = adminRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return new AdminResponseDTO(token, admin.getName());
    }

    @Override
    public void createDefaultAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setName("Super Admin");
            admin.setEmail("admin@company.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            System.out.println("✅ Default admin created → admin@company.com / admin123");
        }
    }
}
