////package com.survey.controller;
////
////import java.util.Map;
////
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestBody;
////import org.springframework.web.bind.annotation.RequestMapping;
////import org.springframework.web.bind.annotation.RestController;
////
////import com.survey.dto.request.AdminLoginDTO;
////import com.survey.dto.request.ForgotPasswordRequestDTO;
////import com.survey.dto.request.ResetPasswordDTO;
////import com.survey.dto.request.VerifyOtpDTO;
////import com.survey.dto.response.AdminResponseDTO;
////import com.survey.service.AuthService;
////
////import jakarta.validation.Valid;
////import lombok.RequiredArgsConstructor;
//////
//////@RestController
//////@RequestMapping("/api/auth")
//////@RequiredArgsConstructor
//////public class AuthController {
//////
//////    private final AuthService authService;
//////
//////    @PostMapping("/login")
//////    public ResponseEntity<AdminResponseDTO> login(@Valid @RequestBody AdminLoginDTO dto) {
//////        return ResponseEntity.ok(authService.login(dto));
//////    }
//////    
//////    @PostMapping("/forgot-password")
//////    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
//////        authService.sendResetOtp(dto.getEmail());
//////        return ResponseEntity.ok("OTP sent to email.");
//////    }
//////
//////    @PostMapping("/verify-otp")
//////    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpDTO dto) {
//////        boolean valid = authService.verifyOtp(dto.getEmail(), dto.getOtp());
//////        return valid ? ResponseEntity.ok("OTP Verified") :
//////                ResponseEntity.badRequest().body("Invalid or expired OTP");
//////    }
//////
//////    @PostMapping("/reset-password")
//////    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
//////        authService.resetPassword(dto.getEmail(), dto.getNewPassword());
//////        return ResponseEntity.ok("Password updated successfully.");
//////    }
//////
//////}
////
////
////@RestController
////@RequestMapping("/api/auth")
////@RequiredArgsConstructor
////public class AuthController {
////
////    private final AuthService authService;
////
////    @PostMapping("/login")
////    public ResponseEntity<AdminResponseDTO> login(@Valid @RequestBody AdminLoginDTO dto) {
////        return ResponseEntity.ok(authService.login(dto));
////    }
////
////    @PostMapping("/forgot-password")
////    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
////        authService.sendResetOtp(dto.getEmail());
////        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
////    }
////
////
////    @PostMapping("/verify-otp")
////    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpDTO dto) {
////        boolean valid = authService.verifyOtp(dto.getEmail(), dto.getOtp());
////        return valid ? ResponseEntity.ok("OTP Verified!") :
////                ResponseEntity.badRequest().body("Invalid or expired OTP");
////    }
////
////    @PostMapping("/reset-password")
////    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
////        authService.resetPassword(dto.getEmail(), dto.getNewPassword());
////        return ResponseEntity.ok("Password updated successfully!");
////    }
////}
////
//
//package com.survey.controller;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.survey.dto.request.AdminLoginDTO;
//import com.survey.dto.request.AdminPasswordChangeRequestDTO;
//import com.survey.dto.request.AdminUpdateRequestDTO;
//import com.survey.dto.request.ForgotPasswordRequestDTO;
//import com.survey.dto.request.ResetPasswordDTO;
//import com.survey.dto.request.VerifyOtpDTO;
//import com.survey.dto.response.AdminResponseDTO;
//import com.survey.entity.Admin;
//import com.survey.repository.AdminRepository;
//import com.survey.service.AuthService;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//	private final AuthService authService;
//	private final AdminRepository adminRepository;
//
////    // ------------------------------------------------------------
////    // LOGIN
////    // ------------------------------------------------------------
////    @PostMapping("/login")
////    public ResponseEntity<AdminResponseDTO> login(@Valid @RequestBody AdminLoginDTO dto) {
////        return ResponseEntity.ok(authService.login(dto));
////    }
////
////    // ------------------------------------------------------------
////    // FORGOT PASSWORD - SEND OTP
////    // ------------------------------------------------------------
////    @PostMapping("/forgot-password")
////    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
////        authService.sendResetOtp(dto.getEmail());
////        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
////    }
////
////    // ------------------------------------------------------------
////    // VERIFY OTP
////    // ------------------------------------------------------------
////    @PostMapping("/verify-otp")
////    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpDTO dto) {
////        boolean valid = authService.verifyOtp(dto.getEmail(), dto.getOtp());
////        return valid ?
////                ResponseEntity.ok("OTP Verified!") :
////                ResponseEntity.badRequest().body("Invalid or expired OTP");
////    }
////
////    // ------------------------------------------------------------
////    // RESET PASSWORD
////    // ------------------------------------------------------------
////    @PostMapping("/reset-password")
////    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
////        authService.resetPassword(dto.getEmail(), dto.getNewPassword());
////        return ResponseEntity.ok("Password updated successfully!");
////    }
////
////    // ------------------------------------------------------------
////    // GET LOGGED-IN ADMIN PROFILE  (used by profile page)
////    // ------------------------------------------------------------
////    @GetMapping("/me")
////    public ResponseEntity<?> getLoggedInUser(HttpServletRequest request) {
////
////        String email = (String) request.getAttribute("email");
////        if (email == null) {
////            return ResponseEntity.status(401).body("Not authenticated");
////        }
////
////        Admin admin = adminRepository.findByEmail(email).orElse(null);
////        if (admin == null) {
////            return ResponseEntity.status(404).body("Admin not found");
////        }
////
////        return ResponseEntity.ok(Map.of(
////                "id", admin.getId(),
////                "name", admin.getName(),
////                "email", admin.getEmail(),
////                "role", "Admin",
////                "department", "Human Resources",
////                "profileImage", null
////        ));
////    }
////
////    
////    // UPDATE PROFILE -----------------------------------------------
////    @PutMapping("/update-profile")
////    public ResponseEntity<?> updateProfile(HttpServletRequest request,
////                                           @RequestBody AdminUpdateRequestDTO dto) {
////
////        String email = (String) request.getAttribute("email");
////        return ResponseEntity.ok(authService.updateProfile(email, dto));
////    }
////
////    // CHANGE PASSWORD ----------------------------------------------
////    @PostMapping("/change-password")
////    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
////
////        String email = body.get("email");
////        String currentPassword = body.get("currentPassword");
////        String newPassword = body.get("newPassword");
////
////        authService.changePassword(email, currentPassword, newPassword);
////
////        return ResponseEntity.ok(Map.of("message", "Password updated successfully!"));
////    }
////
////    // UPLOAD IMAGE -------------------------------------------------
////    @PostMapping("/upload-image")
////    public ResponseEntity<?> uploadImage(
////            HttpServletRequest request,
////            @RequestParam("file") MultipartFile file) throws Exception {
////
////        String email = (String) request.getAttribute("email");
////
////        String imageName = authService.uploadProfileImage(email, file);
////
////        return ResponseEntity.ok(Map.of("image", imageName));
////    }
//
//	// ---------------- LOGIN ----------------
//	@PostMapping("/login")
//	public ResponseEntity<AdminResponseDTO> login(@Valid @RequestBody AdminLoginDTO dto) {
//		return ResponseEntity.ok(authService.login(dto));
//	}
//
//	// ---------------- FORGOT PASSWORD ----------------
//	@PostMapping("/forgot-password")
//	public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
//		authService.sendResetOtp(dto.getEmail());
//		return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
//	}
//
//	// ---------------- VERIFY OTP ----------------
//	@PostMapping("/verify-otp")
//	public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpDTO dto) {
//		boolean valid = authService.verifyOtp(dto.getEmail(), dto.getOtp());
//		return valid ? ResponseEntity.ok("OTP Verified!") : ResponseEntity.badRequest().body("Invalid or expired OTP");
//	}
//
//	// ---------------- RESET PASSWORD ----------------
//	@PostMapping("/reset-password")
//	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
//		authService.resetPassword(dto.getEmail(), dto.getNewPassword());
//		return ResponseEntity.ok("Password updated successfully!");
//	}
//
//	// ---------------- GET PROFILE ----------------
//	@GetMapping("/me")
//	public ResponseEntity<?> getLoggedInUser(HttpServletRequest request) {
//
//	    String email = (String) request.getAttribute("email");
//	    if (email == null)
//	        return ResponseEntity.status(401).body("Not authenticated");
//
//	    Admin admin = adminRepository.findByEmail(email).orElse(null);
//	    if (admin == null)
//	        return ResponseEntity.status(404).body("Admin not found");
//
//	    Map<String, Object> map = new HashMap<>();
//	    map.put("id", admin.getId());
//	    map.put("name", admin.getName());
//	    map.put("email", admin.getEmail());
//	    map.put("role", "Admin");
//	    map.put("department", "Human Resources");
//	    map.put("profileImage", admin.getProfileImage());
//
//	    return ResponseEntity.ok(map);
//	}
//
//
//
//	// ---------------- UPDATE PROFILE ----------------
//	@PutMapping("/update-profile")
//	public ResponseEntity<?> updateProfile(HttpServletRequest request,
//	                                       @RequestBody AdminUpdateRequestDTO dto) {
//
//	    String email = (String) request.getAttribute("email");
//	    return ResponseEntity.ok(authService.updateProfile(email, dto));
//	}
//
//	// ---------------- CHANGE PASSWORD ----------------
//	@PostMapping("/change-password")
//	public ResponseEntity<?> changePassword(HttpServletRequest request,
//	                                        @RequestBody AdminPasswordChangeRequestDTO dto) {
//
//	    String email = (String) request.getAttribute("email");
//	    authService.changePassword(email, dto.getOldPassword(), dto.getNewPassword());
//	    return ResponseEntity.ok(Map.of("message", "Password updated successfully!"));
//	}
//
//
//	// ---------------- UPLOAD IMAGE ----------------
//	@PostMapping("/upload-image")
//	public ResponseEntity<?> uploadImage(HttpServletRequest request,
//	                                     @RequestParam("file") MultipartFile file)
//	        throws Exception {
//
//	    String email = (String) request.getAttribute("email");
//	    String imageName = authService.uploadProfileImage(email, file);
//	    return ResponseEntity.ok(Map.of("image", imageName));
//	}
//
//}
package com.survey.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.survey.dto.request.*;
import com.survey.dto.response.AdminResponseDTO;
import com.survey.entity.Admin;
import com.survey.repository.AdminRepository;
import com.survey.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AdminRepository adminRepository;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AdminResponseDTO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // PUBLIC: Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        authService.sendResetOtp(dto.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    // PUBLIC: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpDTO dto) {
        boolean valid = authService.verifyOtp(dto.getEmail(), dto.getOtp());
        return valid ? ResponseEntity.ok("OTP Verified!") :
                ResponseEntity.badRequest().body("Invalid or expired OTP");
    }

    // PUBLIC: Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto.getEmail(), dto.getNewPassword());
        return ResponseEntity.ok("Password updated successfully!");
    }

    // LOGGED-IN USER PROFILE
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(HttpServletRequest req) {

        String email = (String) req.getAttribute("email");
        if (email == null) return ResponseEntity.status(401).body("Not authenticated");

        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin == null) return ResponseEntity.status(404).body("Admin not found");

        return ResponseEntity.ok(Map.of(
                "id", admin.getId(),
                "name", admin.getName(),
                "email", admin.getEmail(),
                "role", "Admin",
                "department", "Human Resources",
                "profileImage", admin.getProfileImage()
        ));
    }

    // UPDATE PROFILE
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest req,
                                           @RequestBody AdminUpdateRequestDTO dto) {

        String email = (String) req.getAttribute("email");
        return ResponseEntity.ok(authService.updateProfile(email, dto));
    }

    // CHANGE PASSWORD
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request,
                                            @RequestBody AdminPasswordChangeRequestDTO dto) {

        String email = (String) request.getAttribute("email");

        if (email == null) {
            return ResponseEntity.status(401).body("Unauthorized: Email missing in request");
        }

        try {
            authService.changePassword(email, dto.getOldPassword(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }


    // UPLOAD IMAGE
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(HttpServletRequest req,
                                         @RequestParam("file") MultipartFile file) throws Exception {

        String email = (String) req.getAttribute("email");
        String img = authService.uploadProfileImage(email, file);

        return ResponseEntity.ok(Map.of("image", img));
    }
}
