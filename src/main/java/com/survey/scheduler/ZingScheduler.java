//package com.survey.scheduler;
//
//import com.survey.serviceImpl.ZingHRFetchService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class ZingScheduler {
//
//    private final ZingHRFetchService service;
//
////    @Scheduled(cron = "0 0 * * * *")
//    public void autoFetch() {
//        service.fetchEmployeesAndSend();
//    }
//}


package com.survey.scheduler;

import com.survey.serviceImpl.ZingHRFetchService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ZingScheduler {

    private final ZingHRFetchService service;

    @Value("${zinghr.test-mode:true}")
    private boolean TEST_MODE;

    @Scheduled(cron = "0 0 * * * *")
    public void autoFetch() {

        if (TEST_MODE) {
            System.out.println("TEST MODE — Scheduler is disabled.");
            return;
        }

        service.fetchEmployeesAndSend();
    }
}
