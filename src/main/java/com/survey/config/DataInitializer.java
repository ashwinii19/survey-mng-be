package com.survey.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.survey.entity.Admin;
import com.survey.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        String adminEmail = "admin@survey.com";
        String adminPassword = "admin123";

        if (!adminRepository.findByEmail(adminEmail).isPresent()) {
            Admin admin = new Admin();
            admin.setName("Super Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            adminRepository.save(admin);

            System.out.println("======================================================");
            System.out.println("✅ Default Admin user created successfully!");
            System.out.println("Email: " + adminEmail);
            System.out.println("Password: " + adminPassword + "  (raw password for first login)");
            System.out.println("======================================================");
        } else {
            System.out.println("🔹 Default admin already exists. Skipping creation.");
        }
    }
}
