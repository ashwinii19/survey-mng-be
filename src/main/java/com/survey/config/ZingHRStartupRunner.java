//package com.survey.config;
//
//import com.survey.serviceImpl.ZingHRFetchService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class ZingHRStartupRunner implements ApplicationRunner {
//
//    private final ZingHRFetchService zingHRFetchService;
//
//    @Override
//    public void run(ApplicationArguments args) {
//        log.info("🚀 Triggering ZingHR sync on application startup...");
//        zingHRFetchService.fetchEmployeesAndSend();
//    }
//}



package com.survey.config;

import com.survey.serviceImpl.ZingHRFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ZingHRStartupRunner implements ApplicationRunner {

    private final ZingHRFetchService zingHRFetchService;

    /**
     * Controls whether ZingHR sync runs automatically on application startup.
     * Can be configured via application.properties:
     * zinghr.auto-run=true
     */
    @Value("${zinghr.auto-run:false}")
    private boolean autoRun;

    @Override
    public void run(ApplicationArguments args) {

        log.info("📌 ZingHRStartupRunner: autoRun = {}", autoRun);

        if (!autoRun) {
            log.info("🚫 ZingHR auto-run disabled. Skipping sync.");
            return;
        }

        log.info("🚀 ZingHR auto-run enabled. Starting employee sync...");
        try {
            zingHRFetchService.fetchEmployeesAndSend();
            log.info("✅ ZingHR sync completed successfully.");
        } catch (Exception e) {
            log.error("❌ ZingHR sync failed: {}", e.getMessage(), e);
        }
    }
}
