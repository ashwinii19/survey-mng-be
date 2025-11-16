package com.survey.serviceImpl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.survey.dto.request.AdminLoginDTO;
import com.survey.dto.request.AdminUpdateRequestDTO;
import com.survey.dto.response.AdminProfileResponseDTO;
import com.survey.dto.response.AdminResponseDTO;
import com.survey.entity.Admin;
import com.survey.repository.AdminRepository;
import com.survey.security.JwtTokenProvider;
import com.survey.service.AuthService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService; 

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
            admin.setEmail("admin@survey.com"); // FIXED
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            System.out.println("✅ Default admin created → admin@survey.com / admin123");
        }
    }

//    
//
//    @Override
//    public void sendResetOtp(String email) {
//        Admin admin = adminRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Email not found"));
//
//        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
//
//        admin.setResetOtp(otp);
//        admin.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
//        adminRepository.save(admin);
//
//       
//        emailService.sendSimpleMail(
//                email,
//                "Your Password Reset OTP",
//                "Your OTP for resetting password is: " + otp + "\nIt is valid for 10 minutes."
//        );
//    }
//
//    @Override
//    public boolean verifyOtp(String email, String otp) {
//        Admin admin = adminRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Email not found"));
//
//        if (admin.getResetOtp() == null) return false;
//        if (!admin.getResetOtp().equals(otp)) return false;
//        if (admin.getOtpExpiry().isBefore(LocalDateTime.now())) return false;
//
//        return true;
//    }
//
//    @Override
//    public void resetPassword(String email, String newPassword) {
//        Admin admin = adminRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Email not found"));
//
//        admin.setPassword(passwordEncoder.encode(newPassword)); // FIXED ✔
//        admin.setResetOtp(null);
//        admin.setOtpExpiry(null);
//
//        adminRepository.save(admin);
//    }
//
//}



    @Override
    public void sendResetOtp(String email) {

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        admin.setResetOtp(otp);
        admin.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        adminRepository.save(admin);

        emailService.sendSimpleMail(
                email,
                "Your Password Reset OTP",
                "Your OTP is: " + otp + "\nValid for 10 minutes"
        );
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        return admin.getResetOtp() != null &&
               admin.getResetOtp().equals(otp) &&
               admin.getOtpExpiry().isAfter(LocalDateTime.now());
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setResetOtp(null);
        admin.setOtpExpiry(null);

        adminRepository.save(admin);
    }
    
    @Override
    public AdminProfileResponseDTO updateProfile(String email, AdminUpdateRequestDTO dto) {

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setUpdatedAt(LocalDateTime.now());

        adminRepository.save(admin);

        AdminProfileResponseDTO res = new AdminProfileResponseDTO();
        res.setId(admin.getId());
        res.setName(admin.getName());
        res.setEmail(admin.getEmail());
        res.setRole("Admin");
        res.setProfileImage(admin.getProfileImage());

        return res;
    }

    // CHANGE PASSWORD ----------------------------------------------
    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }



    // UPLOAD IMAGE --------------------------------------------------
    @Override
    public String uploadProfileImage(String email, MultipartFile file) throws Exception {

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String fileName = email.replace("@", "_") + "_" + System.currentTimeMillis() + ".png";

        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        Files.copy(file.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        admin.setProfileImage(fileName);
        adminRepository.save(admin);

        return fileName;
    }

}
