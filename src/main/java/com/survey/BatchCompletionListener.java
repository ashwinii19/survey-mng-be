package com.survey;

import com.survey.service.OnboardingEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BatchCompletionListener implements JobExecutionListener {
    
    @Autowired
    private OnboardingEmailService onboardingEmailService;

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("🎯🎯🎯 JOB EXECUTION LISTENER FIRED!");
        
        if ("importEmployeeJob".equals(jobExecution.getJobInstance().getJobName())) {
            
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                
                Long batchLogId = jobExecution.getJobParameters().getLong("batchLogId");
                String sendWelcomeEmail = jobExecution.getJobParameters().getString("sendWelcomeEmail");
                
                log.info("🎯 Employee import job completed. BatchLogId: {}, SendEmail: {}", 
                        batchLogId, sendWelcomeEmail);
                
                if ("true".equals(sendWelcomeEmail) && batchLogId != null) {
                    log.info("🚀 Auto-triggering onboarding welcome emails for batch: {}", batchLogId);
                    onboardingEmailService.sendWelcomeEmailsForBatch(batchLogId);
                }
            }
        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Optional - called before job starts
    }
}